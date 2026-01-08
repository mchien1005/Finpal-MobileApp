import 'dart:async';

/// Simple event bus for transaction list updates.
/// Now carries an optional transaction id so listeners can react (e.g. move updated item to top).
class TransactionEventBus {
  TransactionEventBus._internal();
  static final TransactionEventBus instance = TransactionEventBus._internal();

  final StreamController<int?> _controller = StreamController<int?>.broadcast();

  /// Notify listeners that transactions changed (created/updated/deleted)
  /// If `transactionId` is provided, listeners can prioritize that item.
  void notifyUpdated([int? transactionId]) => _controller.add(transactionId);

  /// Stream to listen for updates. Emits `int?` (transaction id or null).
  Stream<int?> get onUpdated => _controller.stream;

  void dispose() {
    _controller.close();
  }
}
