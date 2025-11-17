import 'package:flutter/foundation.dart';
import '../services/auth_service.dart';

class AuthProvider with ChangeNotifier {
  final AuthService _authService = AuthService();
  bool _isAuthenticated = false;
  String? _userId;
  String? _username;

  bool get isAuthenticated => _isAuthenticated;
  String? get userId => _userId;
  String? get username => _username;

  AuthProvider() {
    _checkAuthStatus();
  }

  Future<void> _checkAuthStatus() async {
    final token = await _authService.getToken();
    final userId = await _authService.getUserId();
    if (token != null && userId != null) {
      _isAuthenticated = true;
      _userId = userId;
      notifyListeners();
    }
  }

  Future<bool> login(String username, String password) async {
    final result = await _authService.login(username, password);
    if (result['success'] == true) {
      _isAuthenticated = true;
      _userId = result['data']['userId'].toString();
      _username = username;
      notifyListeners();
      return true;
    }
    // Store error message for display
    _errorMessage = result['message'] ?? 'Login failed';
    notifyListeners();
    return false;
  }

  String? _errorMessage;
  String? get errorMessage => _errorMessage;

  Future<void> logout() async {
    await _authService.logout();
    _isAuthenticated = false;
    _userId = null;
    _username = null;
    notifyListeners();
  }
}

