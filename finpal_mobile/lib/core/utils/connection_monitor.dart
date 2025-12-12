import 'dart:async';
import 'package:flutter/material.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import '../widgets/connection_lost_dialog.dart';

/// Wrap any screen with this to auto-show a dialog when offline
class ConnectionMonitor extends StatefulWidget {
  final Widget child;
  final Future<bool> Function()? onRetry; // return true when reconnected

  const ConnectionMonitor({super.key, required this.child, this.onRetry});

  @override
  State<ConnectionMonitor> createState() => _ConnectionMonitorState();
}

class _ConnectionMonitorState extends State<ConnectionMonitor> {
  late final Connectivity _connectivity;
  StreamSubscription<List<ConnectivityResult>>? _subscription;
  bool _dialogShown = false;

  @override
  void initState() {
    super.initState();
    _connectivity = Connectivity();
    _subscription = _connectivity.onConnectivityChanged.listen(_onStatus);
    _initialCheck();
  }

  Future<void> _initialCheck() async {
    final results = await _connectivity.checkConnectivity();
    _onStatus(results);
  }

  void _onStatus(List<ConnectivityResult> results) {
    final isOffline = results.every((r) => r == ConnectivityResult.none);
    if (isOffline && !_dialogShown && mounted) {
      _dialogShown = true;
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) => ConnectionLostDialog(
          onRetry: () async {
            // try retry callback first
            bool ok = false;
            if (widget.onRetry != null) {
              ok = await widget.onRetry!.call();
            } else {
              final now = await _connectivity.checkConnectivity();
              ok = now.any((r) => r != ConnectivityResult.none);
            }
            if (ok && mounted) {
              Navigator.of(context).pop();
              _dialogShown = false;
            }
          },
        ),
      );
    }
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => widget.child;
}
