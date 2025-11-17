import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../config/api_config.dart';

class AuthService {
  static String get baseUrl => ApiConfig.baseUrl;
  
  Future<Map<String, dynamic>> login(String username, String password) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/auth/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'username': username,
          'password': password,
        }),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final prefs = await SharedPreferences.getInstance();
        await prefs.setString('token', data['token']);
        await prefs.setString('userId', data['userId'].toString());
        return {'success': true, 'data': data};
      } else {
        // Try to parse error message from response
        String errorMessage = 'Login failed';
        try {
          final errorData = jsonDecode(response.body);
          errorMessage = errorData['message'] ?? errorMessage;
        } catch (e) {
          // If parsing fails, use the raw response body or status code
          errorMessage = response.body.isNotEmpty 
              ? response.body 
              : 'Login failed with status ${response.statusCode}';
        }
        return {
          'success': false,
          'message': errorMessage
        };
      }
    } catch (e) {
      return {
        'success': false, 
        'message': 'Connection error: ${e.toString()}. Please ensure the backend is running at $baseUrl'
      };
    }
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('token');
    await prefs.remove('userId');
  }

  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('token');
  }

  Future<String?> getUserId() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('userId');
  }
}

