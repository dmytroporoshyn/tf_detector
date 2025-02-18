import 'package:flutter/foundation.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:tf_detector/domain/class_result.dart';

import 'tf_detector_method_channel.dart';

abstract class TfDetectorPlatform extends PlatformInterface {
  /// Constructs a TfDetectorPlatform.
  TfDetectorPlatform() : super(token: _token);

  static final Object _token = Object();

  static TfDetectorPlatform _instance = MethodChannelTfDetector();

  /// The default instance of [TfDetectorPlatform] to use.
  ///
  /// Defaults to [MethodChannelTfDetector].
  static TfDetectorPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [TfDetectorPlatform] when
  /// they register themselves.
  static set instance(TfDetectorPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> init() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> close() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> setup({
    required String modelPath,
    double threshold = 0.4,
    int numThreads = 2,
    int maxResults = 3,
    int delegate = 0,
  }) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<List<ClassResult>> detect({
    required List<Uint8List> bytesList,
    required int imageHeight,
    required int imageWidth,
  }) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
