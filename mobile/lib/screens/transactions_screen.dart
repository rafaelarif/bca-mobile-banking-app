import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/api_service.dart';

class TransactionsScreen extends StatefulWidget {
  final String accountId;
  final String accountNumber;

  const TransactionsScreen({
    super.key,
    required this.accountId,
    required this.accountNumber,
  });

  @override
  State<TransactionsScreen> createState() => _TransactionsScreenState();
}

class _TransactionsScreenState extends State<TransactionsScreen> {
  final ApiService _apiService = ApiService();
  List<dynamic> _transactions = [];
  Map<String, dynamic>? _balance;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() {
      _isLoading = true;
    });

    final transactionsResult = await _apiService.getTransactions(widget.accountId);
    final balanceResult = await _apiService.getAccountBalance(widget.accountId);

    setState(() {
      if (transactionsResult['success'] == true) {
        _transactions = transactionsResult['data'] ?? [];
      }
      if (balanceResult['success'] == true) {
        _balance = balanceResult['data'];
      }
      _isLoading = false;
    });

    if (mounted && (transactionsResult['success'] != true || balanceResult['success'] != true)) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Failed to load account information')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Account ${widget.accountNumber}'),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _loadData,
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    if (_balance != null)
                      Container(
                        width: double.infinity,
                        padding: const EdgeInsets.all(24),
                        decoration: const BoxDecoration(
                          color: Color(0xFF2E7D32),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Available Balance',
                              style: TextStyle(
                                color: Colors.white70,
                                fontSize: 14,
                              ),
                            ),
                            const SizedBox(height: 8),
                            Text(
                              '\$${NumberFormat('#,##0.00').format(_balance!['balance'] ?? 0)}',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 32,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                      ),
                    const Padding(
                      padding: EdgeInsets.all(16),
                      child: Text(
                        'Transaction History',
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                    if (_transactions.isEmpty)
                      const Padding(
                        padding: EdgeInsets.all(24),
                        child: Center(child: Text('No transactions found')),
                      )
                    else
                      ListView.builder(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        itemCount: _transactions.length,
                        padding: const EdgeInsets.symmetric(horizontal: 16),
                        itemBuilder: (context, index) {
                          final transaction = _transactions[index];
                          final isCredit = transaction['type'] == 'CREDIT' ||
                              transaction['type'] == 'DEPOSIT';
                          final amount = transaction['amount'] ?? 0.0;

                          return Card(
                            margin: const EdgeInsets.only(bottom: 8),
                            child: ListTile(
                              leading: CircleAvatar(
                                backgroundColor: isCredit
                                    ? Colors.green.shade100
                                    : Colors.red.shade100,
                                child: Icon(
                                  isCredit ? Icons.arrow_downward : Icons.arrow_upward,
                                  color: isCredit ? Colors.green : Colors.red,
                                ),
                              ),
                              title: Text(
                                transaction['description'] ?? 'Transaction',
                                style: const TextStyle(fontWeight: FontWeight.bold),
                              ),
                              subtitle: Text(
                                DateFormat('MMM d, y • h:mm a').format(
                                  DateTime.parse(transaction['date'] ?? DateTime.now().toIso8601String()),
                                ),
                              ),
                              trailing: Text(
                                '${isCredit ? '+' : '-'}\$${NumberFormat('#,##0.00').format(amount)}',
                                style: TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                  color: isCredit ? Colors.green : Colors.red,
                                ),
                              ),
                            ),
                          );
                        },
                      ),
                  ],
                ),
              ),
            ),
    );
  }
}

