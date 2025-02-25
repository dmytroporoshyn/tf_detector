import Flutter
import UIKit

import TensorFlowLite

enum TFDetectorError: Error {
    case invalidArguments
}


public class TfDetectorPlugin: NSObject, FlutterPlugin {
    
    private var objectDetector: ObjectDetector?
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "tf_detector", binaryMessenger: registrar.messenger())
        let instance = TfDetectorPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "init":
            result(true)
        case "setup":
            do {
                guard let args = call.arguments else {
                    throw TFDetectorError.invalidArguments
                }
                let myArgs = args as? [String: Any?]
                let model = myArgs?["model"] as! String
                let threshold = (myArgs?["threshold"] as! NSNumber).floatValue
                let numThreads = myArgs?["numThreads"] as! Int
                let maxResults = myArgs?["maxResults"] as! Int
                
                objectDetector = ObjectDetector(modelPath: model, threadCount: numThreads)
                result(true)
            } catch {
                print("Failed to initialize TFLite interpreter: \(error)")
                result(error)
            }
        case "detect":
            do {
                guard let args = call.arguments else {
                    throw TFDetectorError.invalidArguments
                }
                let myArgs = args as? [String: Any?]
                let base64 = myArgs?["bytesList"] as! String
                let imageHeight = myArgs?["imageHeight"] as! Int
                let imageWidth = myArgs?["imageWidth"] as! Int
                
                let dataDecoded : Data = Data(base64Encoded: base64, options: .ignoreUnknownCharacters)!
                
                let image = UIImage(data: dataDecoded)
                let targetSize = objectDetector?.getModelInputSize()
                
                print(image)
                print(targetSize)
//                let resizedImage = resizeImage(image, targetSize: targetSize)
//                let imageData = resizedImage.pngData()!
                
//                let data = bytesList.data
//                objectDetector?.detectObjects(in: data)
                result(true)
            } catch {
                print("Failed to detect: \(error)")
                result(error)
            }
        case "close":
            result(true)
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    func resizeImage(_ image: UIImage, targetSize: CGSize) -> UIImage {
        let renderer = UIGraphicsImageRenderer(size: targetSize)
        return renderer.image { _ in
            image.draw(in: CGRect(origin: .zero, size: targetSize))
        }
    }
}

class ObjectDetector {
    private var interpreter: Interpreter
    
    init?(modelPath: String, threadCount: Int) {
        do {
            var options = Interpreter.Options()
            options.threadCount = 2 // Example of setting options
            
            interpreter = try Interpreter(modelPath: modelPath, options: options)
            try interpreter.allocateTensors()
        } catch {
            print("Failed to initialize TFLite interpreter: \(error)")
            return nil
        }
    }
    
    func getModelInputSize() -> CGSize? {
        do {
            let inputTensor = try interpreter.input(at: 0)
            let shape = inputTensor.shape
            guard shape.dimensions.count >= 4 else { return nil }
            
            return CGSize(width: shape.dimensions[1], height: shape.dimensions[2])
        } catch {
            print("Error retrieving model input size: \(error)")
            return nil
        }
    }
    
    func detectObjects(in imageData: Data) {
        do {
            let inputTensor = try interpreter.input(at: 0)
            try interpreter.copy(imageData, toInputAt: 0)
            try interpreter.invoke()
            
            let outputTensor = try interpreter.output(at: 0)
            let results = parseOutput(outputTensor)
            print(results)
        } catch {
            print("Error running model: \(error)")
        }
    }
    
    private func parseOutput(_ outputTensor: Tensor) {
        let outputData = outputTensor.data
        // Process the output data according to your model's output format
    }
}
