# What's My Breed
## Description: 
This project implements convolutional neural networks and transfer learning to classify cat and dog breeds from images. The trained model will, hopefully, be deployed on a mobile app for real-time detection.


## Core Functionality: 
- Real-time breed recognition via camera
- Breed recognition from static image/video
- Display animal (cat/dog) and breed name
- Future: Display breed info + fun stats

## Technical Details 
- CNN: MobileNetV2 → small and fast which makes it good for mobile applications. 
- Datasets:
  - Stanford Dogs Dataset: 
      120 dog breeds (~20,580 images)
  - Oxford-IIIT Pet Dataset: 
      12 cat breeds and 25 dog breeds (7,349 images)
    
- Tech Stack:
  - Keras → Dataset Preprocessing
  - TensorFlow → Model training
  - TFLite → converting for mobile use
  - Kotlin → Mobile app development
