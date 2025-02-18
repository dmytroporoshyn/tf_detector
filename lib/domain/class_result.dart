class ClassResult {
  ClassResult({
    required this.rectResult,
    required this.label,
    required this.score,
  });

  ClassResult.fromJson(Map<String, dynamic> json)
      : this(
          rectResult: RectResult.fromJson(
            (json['rect'] as Map).cast<String, dynamic>(),
          ),
          score: json['score'] as double,
          label: json['label'] as String,
        );

  final RectResult rectResult;
  final double score;
  final String label;
}

class RectResult {
  RectResult({
    required this.top,
    required this.right,
    required this.bottom,
    required this.left,
  });

  RectResult.fromJson(Map<String, dynamic> json)
      : this(
          top: json['top'] as double,
          right: json['right'] as double,
          bottom: json['bottom'] as double,
          left: json['left'] as double,
        );

  final double top;
  final double right;
  final double bottom;
  final double left;

// Rect toRect(int imageWidth, int imageHeight, {int offset = 5}) {
//   final left = imageWidth * x - offset;
//   final top = imageHeight * y - offset;
//   final width = imageWidth * w + offset;
//   final height = imageHeight * h + offset;
//   return Rect.fromLTWH(left, top, width, height);
// }
}
