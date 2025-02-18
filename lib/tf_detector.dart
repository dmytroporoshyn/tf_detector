import 'package:flutter/foundation.dart';
import 'package:tf_detector/domain/class_result.dart';

import 'tf_detector_platform_interface.dart';

class TfDetector {
  Future<void> init() {
    return TfDetectorPlatform.instance.init();
  }

  Future<void> close() {
    return TfDetectorPlatform.instance.close();
  }

  Future<void> setup({
    required String modelPath,
    double threshold = 0.4,
    int numThreads = 2,
    int maxResults = 3,
    int delegate = 0,
  }) {
    return TfDetectorPlatform.instance.setup(
      modelPath: modelPath,
      maxResults: maxResults,
      numThreads: numThreads,
      threshold: threshold,
      delegate: delegate,
    );
  }

  Future<List<ClassResult>> detect({
    required List<Uint8List> bytesList,
    required int imageHeight,
    required int imageWidth,
  }) {
    return TfDetectorPlatform.instance.detect(
      bytesList: bytesList,
      imageHeight: imageHeight,
      imageWidth: imageWidth,
    );
  }
}
