import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/api_service.dart';

class BillPaymentScreen extends StatefulWidget {
  const BillPaymentScreen({super.key});

  @override
  State<BillPaymentScreen> createState() => _BillPaymentScreenState();
}

class _BillPaymentScreenState extends State<BillPaymentScreen> {
  final ApiService _apiService = ApiService();
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  final _memoController = TextEditingController();
  
  List<dynamic> _accounts = [];
  List<dynamic> _payees = [];
  dynamic _selectedAccount;
  dynamic _selectedPayee;
  bool _isLoading = true;
  bool _isSubmitting = false;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  @override
  void dispose() {
    _amountController.dispose();
    _memoController.dispose();
    super.dispose();
  }

  Future<void> _loadData() async {
    setState(() {
      _isLoading = true;
    });

    final accountsResult = await _apiService.getAccounts();
    final payeesResult = await _apiService.getPayees();

    setState(() {
      if (accountsResult['success'] == true) {
        _accounts = accountsResult['data'] ?? [];
      }
      if (payeesResult['success'] == true) {
        _payees = payeesResult['data'] ?? [];
      }
      _isLoading = false;
    });

    if (mounted) {
      if (accountsResult['success'] != true || payeesResult['success'] != true) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Failed to load data')),
        );
      }
    }
  }

  Future<void> _showConfirmation() async {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedAccount == null || _selectedPayee == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select account and payee')),
      );
      return;
    }

    final amount = double.tryParse(_amountController.text);
    if (amount == null || amount <= 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter a valid amount')),
      );
      return;
    }

    if (amount > (_selectedAccount['balance'] as num).toDouble()) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Insufficient balance')),
      );
      return;
    }

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Confirm Bill Payment'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Payee: ${_selectedPayee['name']}'),
            Text('Account: ${_selectedAccount['accountNumber']}'),
            const SizedBox(height: 8),
            Text('Amount: \$${NumberFormat('#,##0.00').format(amount)}'),
            if (_memoController.text.isNotEmpty)
              Text('Memo: ${_memoController.text}'),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF2E7D32),
              foregroundColor: Colors.white,
            ),
            child: const Text('Confirm'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      _processPayment(amount);
    }
  }

  Future<void> _processPayment(double amount) async {
    setState(() {
      _isSubmitting = true;
    });

    final result = await _apiService.processBillPayment(
      _selectedAccount['id'].toString(),
      _selectedPayee['id'].toString(),
      amount,
      _memoController.text.isEmpty ? null : _memoController.text,
    );

    setState(() {
      _isSubmitting = false;
    });

    if (result['success'] == true) {
      if (mounted) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (context) => BillPaymentSuccessScreen(
              payeeName: _selectedPayee['name'],
              amount: amount,
            ),
          ),
        );
      }
    } else {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(result['message'] ?? 'Payment failed')),
        );
      }
    }
  }

  void _showManagePayees() {
    showDialog(
      context: context,
      builder: (context) => _ManagePayeesDialog(
        apiService: _apiService,
        payees: _payees,
        onPayeeAdded: () {
          _loadData();
        },
        onPayeeDeleted: () {
          _loadData();
          setState(() {
            _selectedPayee = null;
          });
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Bill Payments'),
        actions: [
          IconButton(
            icon: const Icon(Icons.manage_accounts),
            onPressed: _showManagePayees,
            tooltip: 'Manage Payees',
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Select Payee',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        TextButton.icon(
                          onPressed: _showManagePayees,
                          icon: const Icon(Icons.add, size: 18),
                          label: const Text('Manage'),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    DropdownButtonFormField<dynamic>(
                      initialValue: _selectedPayee,
                      decoration: const InputDecoration(
                        border: OutlineInputBorder(),
                        prefixIcon: Icon(Icons.person),
                      ),
                      items: _payees.map((payee) {
                        return DropdownMenuItem(
                          value: payee,
                          child: Text(
                            payee['category'] != null && payee['category'].toString().isNotEmpty
                                ? '${payee['name']} (${payee['category']})'
                                : payee['name'],
                            style: const TextStyle(fontWeight: FontWeight.bold),
                            overflow: TextOverflow.ellipsis,
                          ),
                        );
                      }).toList(),
                      onChanged: (value) {
                        setState(() {
                          _selectedPayee = value;
                        });
                      },
                      validator: (value) {
                        if (value == null) {
                          return 'Please select a payee';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 24),
                    const Text(
                      'From Account',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 8),
                    DropdownButtonFormField<dynamic>(
                      initialValue: _selectedAccount,
                      decoration: const InputDecoration(
                        border: OutlineInputBorder(),
                        prefixIcon: Icon(Icons.account_balance_wallet),
                      ),
                      items: _accounts.map((account) {
                        return DropdownMenuItem(
                          value: account,
                          child: Text(
                            '${account['accountNumber']} - \$${NumberFormat('#,##0.00').format(account['balance'])}',
                          ),
                        );
                      }).toList(),
                      onChanged: (value) {
                        setState(() {
                          _selectedAccount = value;
                        });
                      },
                      validator: (value) {
                        if (value == null) {
                          return 'Please select account';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 24),
                    TextFormField(
                      controller: _amountController,
                      decoration: const InputDecoration(
                        labelText: 'Amount',
                        border: OutlineInputBorder(),
                        prefixIcon: Icon(Icons.attach_money),
                        prefixText: '\$ ',
                      ),
                      keyboardType: const TextInputType.numberWithOptions(decimal: true),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter amount';
                        }
                        final amount = double.tryParse(value);
                        if (amount == null || amount <= 0) {
                          return 'Please enter a valid amount';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 24),
                    TextFormField(
                      controller: _memoController,
                      decoration: const InputDecoration(
                        labelText: 'Memo (Optional)',
                        border: OutlineInputBorder(),
                        prefixIcon: Icon(Icons.note),
                      ),
                      maxLines: 2,
                    ),
                    const SizedBox(height: 32),
                    ElevatedButton(
                      onPressed: _isSubmitting ? null : _showConfirmation,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFF2E7D32),
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 16),
                      ),
                      child: _isSubmitting
                          ? const SizedBox(
                              height: 20,
                              width: 20,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                              ),
                            )
                          : const Text(
                              'Pay Bill',
                              style: TextStyle(fontSize: 16),
                            ),
                    ),
                    const SizedBox(height: 16),
                    OutlinedButton.icon(
                      onPressed: _showManagePayees,
                      icon: const Icon(Icons.manage_accounts),
                      label: const Text('Manage Payees'),
                      style: OutlinedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 16),
                      ),
                    ),
                  ],
                ),
              ),
            ),
    );
  }
}

class BillPaymentSuccessScreen extends StatelessWidget {
  final String payeeName;
  final double amount;

  const BillPaymentSuccessScreen({
    super.key,
    required this.payeeName,
    required this.amount,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Payment Successful'),
        automaticallyImplyLeading: false,
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(
                Icons.check_circle,
                color: Color(0xFF2E7D32),
                size: 80,
              ),
              const SizedBox(height: 24),
              const Text(
                'Payment Processed',
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 32),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Payee: $payeeName'),
                      const SizedBox(height: 8),
                      Text('Amount: \$${NumberFormat('#,##0.00').format(amount)}'),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 32),
              ElevatedButton(
                onPressed: () {
                  Navigator.popUntil(context, (route) => route.isFirst);
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF2E7D32),
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(horizontal: 48, vertical: 16),
                ),
                child: const Text('Done'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ManagePayeesDialog extends StatefulWidget {
  final ApiService apiService;
  final List<dynamic> payees;
  final VoidCallback onPayeeAdded;
  final VoidCallback onPayeeDeleted;

  const _ManagePayeesDialog({
    required this.apiService,
    required this.payees,
    required this.onPayeeAdded,
    required this.onPayeeDeleted,
  });

  @override
  State<_ManagePayeesDialog> createState() => _ManagePayeesDialogState();
}

class _ManagePayeesDialogState extends State<_ManagePayeesDialog> {
  final _nameController = TextEditingController();
  final _accountNumberController = TextEditingController();
  final _categoryController = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  bool _isAdding = false;

  @override
  void dispose() {
    _nameController.dispose();
    _accountNumberController.dispose();
    _categoryController.dispose();
    super.dispose();
  }

  Future<void> _addPayee() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _isAdding = true;
    });

    final result = await widget.apiService.addPayee(
      _nameController.text.trim(),
      _accountNumberController.text.trim().isEmpty ? null : _accountNumberController.text.trim(),
      _categoryController.text.trim().isEmpty ? null : _categoryController.text.trim(),
    );

    setState(() {
      _isAdding = false;
    });

    if (result['success'] == true) {
      _nameController.clear();
      _accountNumberController.clear();
      _categoryController.clear();
      widget.onPayeeAdded();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Payee added successfully')),
        );
      }
    } else {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(result['message'] ?? 'Failed to add payee')),
        );
      }
    }
  }

  Future<void> _deletePayee(dynamic payee) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Payee'),
        content: Text('Are you sure you want to delete ${payee['name']}?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.red,
              foregroundColor: Colors.white,
            ),
            child: const Text('Delete'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      final result = await widget.apiService.deletePayee(payee['id'].toString());
      if (result['success'] == true) {
        widget.onPayeeDeleted();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Payee deleted successfully')),
          );
        }
      } else {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(result['message'] ?? 'Failed to delete payee')),
          );
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      child: Container(
        constraints: const BoxConstraints(maxWidth: 500, maxHeight: 600),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            AppBar(
              title: const Text('Manage Payees'),
              automaticallyImplyLeading: false,
              actions: [
                IconButton(
                  icon: const Icon(Icons.close),
                  onPressed: () => Navigator.pop(context),
                ),
              ],
            ),
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const Text(
                      'Add New Payee',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 16),
                    Form(
                      key: _formKey,
                      child: Column(
                        children: [
                          TextFormField(
                            controller: _nameController,
                            decoration: const InputDecoration(
                              labelText: 'Payee Name *',
                              border: OutlineInputBorder(),
                            ),
                            validator: (value) {
                              if (value == null || value.isEmpty) {
                                return 'Please enter payee name';
                              }
                              return null;
                            },
                          ),
                          const SizedBox(height: 16),
                          TextFormField(
                            controller: _accountNumberController,
                            decoration: const InputDecoration(
                              labelText: 'Account Number (Optional)',
                              border: OutlineInputBorder(),
                            ),
                          ),
                          const SizedBox(height: 16),
                          TextFormField(
                            controller: _categoryController,
                            decoration: const InputDecoration(
                              labelText: 'Category (Optional)',
                              border: OutlineInputBorder(),
                              hintText: 'e.g., UTILITY, CREDIT_CARD',
                            ),
                          ),
                          const SizedBox(height: 16),
                          ElevatedButton(
                            onPressed: _isAdding ? null : _addPayee,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: const Color(0xFF2E7D32),
                              foregroundColor: Colors.white,
                            ),
                            child: _isAdding
                                ? const SizedBox(
                                    height: 20,
                                    width: 20,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                      valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                                    ),
                                  )
                                : const Text('Add Payee'),
                          ),
                        ],
                      ),
                    ),
                    const Divider(height: 32),
                    const Text(
                      'Existing Payees',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 16),
                    if (widget.payees.isEmpty)
                      const Padding(
                        padding: EdgeInsets.all(16),
                        child: Text('No payees found'),
                      )
                    else
                      ...widget.payees.map((payee) => Card(
                            margin: const EdgeInsets.only(bottom: 8),
                            child: ListTile(
                              title: Text(
                                payee['name'],
                                style: const TextStyle(fontWeight: FontWeight.bold),
                              ),
                              subtitle: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  if (payee['accountNumber'] != null)
                                    Text('Account: ${payee['accountNumber']}'),
                                  if (payee['category'] != null)
                                    Text('Category: ${payee['category']}'),
                                ],
                              ),
                              trailing: IconButton(
                                icon: const Icon(Icons.delete, color: Colors.red),
                                onPressed: () => _deletePayee(payee),
                              ),
                            ),
                          )),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

