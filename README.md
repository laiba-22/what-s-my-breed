# What's My Breed
### Description: 
What's My Breed is a mobile app that uses Convolutional Neural Networks (CNNs) and Transfer Learning to classify cat and dog breeds from images — either from the camera or uploaded media. The goal is to deliver real-time, offline breed detection directly on your phone.


### Core Functionality: 
- Real-time breed recognition via camera
- Breed recognition from static images
- Display animal (cat/dog) and breed name
- Future: Display breed info + fun stats

### Technical Details 
- CNN: MobileNetV2 → small and fast which makes it good for mobile applications. 
- Datasets:
  - Stanford Dogs Dataset: 
      120 dog breeds (~20,580 images)
  - Oxford-IIIT Pet Dataset: 
      12 cat breeds and 25 dog breeds (~7,349 images)
    
- Tech Stack:
  - Keras → Dataset Preprocessing
  - TensorFlow → Model training
  - TFLite → converting for mobile use
  - Kotlin → Mobile app development

## Backend
Currently, the backend consists of:
- A Python pipeline to preprocess datasets
- Training scripts for CNN using Keras + TensorFlow
- A TFLite conversion step for mobile deployment


## Frontend
- Libraries & APIs used:
  - Jetpack Compose – for building UI components
  - AndroidX Activity Result APIs – for camera and image uploading features
  - TensorFlow Lite (Android) – to load and run the machine learning model
  - Android APIs (BitmapFactory, InputStream, etc.) – for image handling and preprocessing
- Features
  - Camera Integration - to capture images
  - Image Upload -	lets users pick an image from the gallery 
  - Real-time classifier - uses a pre-trained TensorFlow Lite model to predict the breed
  - Prediction display - shows whether the animal is a cat or dog and displays the predicted breed
  - Compose UI -	easy to use, modern interface using Jetpack Compose.
  - Offline - predictions run locally on device without any need for Internet
