import 'dart:io';
import 'package:flutter/foundation.dart';

class ApiConfig {
  // Base URL configuration
  // IMPORTANT: For physical devices, replace 'localhost' with your computer's IP address
  // Find your IP:
  //   - Mac/Linux: Run `ifconfig | grep "inet " | grep -v 127.0.0.1`
  //   - Windows: Run `ipconfig` and look for IPv4 Address
  // Example: If your IP is 192.168.1.100, change 'localhost' to '192.168.1.100'
  static const String _localIp = 'localhost'; // ⚠️ Change this to your IP if using a physical device
  
  // Get the base URL based on the platform
  static String get baseUrl {
    if (kIsWeb) {
      // Web platform - localhost works
      return 'http://localhost:8080/bca-banking-backend/api';
    } else if (Platform.isAndroid) {
      // Android Emulator: 10.0.2.2 is the special IP to access host machine's localhost
      // Physical Android device: Change _localIp above to your computer's IP address
      // For now, using 10.0.2.2 (works for emulator)
      // If using physical device, change _localIp and replace 10.0.2.2 with _localIp
      if (_localIp == 'localhost') {
        return 'http://10.0.2.2:8080/bca-banking-backend/api';
      } else {
        return 'http://$_localIp:8080/bca-banking-backend/api';
      }
    } else if (Platform.isIOS) {
      // iOS Simulator: localhost works
      // Physical iOS device: Use your computer's IP address (update _localIp above)
      return 'http://$_localIp:8080/bca-banking-backend/api';
    } else {
      // Desktop platforms (Mac, Windows, Linux)
      return 'http://localhost:8080/bca-banking-backend/api';
    }
  }
  
  // Helper method to get instructions for finding IP address
  static String get ipAddressInstructions {
    if (Platform.isMacOS || Platform.isLinux) {
      return 'Run: ifconfig | grep "inet " | grep -v 127.0.0.1';
    } else if (Platform.isWindows) {
      return 'Run: ipconfig | findstr IPv4';
    }
    return 'Check your network settings for your computer\'s IP address';
  }
}

