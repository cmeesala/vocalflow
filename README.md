# VocalFlow

VocalFlow is an Android voice assistant application that uses wake word detection and natural language processing to provide a conversational interface.

## Features

- Wake word detection ("hey luma")
- Continuous command listening
- Natural language processing using OpenAI's GPT-3.5
- Text-to-speech responses
- Sleep word detection ("goodbye")

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/cmeesala/vocalflow.git
   ```

2. Set up your OpenAI API key:
   - Get an API key from [OpenAI](https://platform.openai.com/api-keys)
   - Set it as an environment variable:
     ```bash
     export OPENAI_API_KEY="your-api-key-here"
     ```

3. Open the project in Android Studio

4. Build and run the app on your device or emulator

## Usage

1. Launch the app
2. Say "hey luma" to wake up the assistant
3. Speak your command or question
4. The assistant will respond through both text and speech
5. Say "goodbye" to end the conversation

## Requirements

- Android 8.0 (API level 26) or higher
- Microphone permission
- Internet connection for LLM functionality
- OpenAI API key

## License

This project is licensed under the MIT License - see the LICENSE file for details. 