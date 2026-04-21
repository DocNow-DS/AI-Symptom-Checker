# AI Symptom Checker Service

An AI-powered medical symptom analysis service that provides preliminary health suggestions and doctor specialty recommendations using OpenAI's GPT model.

## Features

- **Symptom Analysis**: Patients can input symptoms and receive AI-powered preliminary analysis
- **Doctor Recommendations**: Suggests appropriate medical specialties based on symptoms
- **Urgency Assessment**: Identifies priority levels (LOW, MEDIUM, HIGH, EMERGENCY)
- **Self-Care Tips**: Provides general wellness advice when appropriate
- **Medical Disclaimer**: Clear warnings that this is not a substitute for professional diagnosis

## Architecture

### Backend (Spring Boot)
- **Port**: 8085
- **Base URL**: `http://localhost:8085`
- **API Endpoint**: `POST /api/symptom-checker/analyze`

### Frontend (React)
- Integrated into the existing healthcare dashboard at `/dashboard/ai-checker`

## Setup Instructions

### 1. Backend Setup

1. Navigate to the AI_Symptom_Checker directory:
```bash
cd AI_Symptom_Checker
```

2. Set your OpenAI API key as an environment variable:
```bash
# Windows PowerShell
$env:OPENAI_API_KEY="your-openai-api-key-here"

# Windows CMD
set OPENAI_API_KEY=your-openai-api-key-here

# Linux/Mac
export OPENAI_API_KEY=your-openai-api-key-here
```

3. Build and run the Spring Boot application:
```bash
./mvnw spring-boot:run
```

Or build the JAR and run:
```bash
./mvnw clean package
java -jar target/AI_Symptom_Checker-0.0.1-SNAPSHOT.jar
```

### 2. Frontend Setup

The frontend expects the AI service to run on `http://localhost:8085` by default. To configure a different URL, create a `.env` file in the `healthCare-Frontend` directory:

```
VITE_AI_SERVICE_URL=http://localhost:8085
```

## API Specification

### Request
```json
{
  "symptoms": "I have a headache and fever for 2 days",
  "age": "28",
  "gender": "male",
  "duration": "2 days",
  "severity": "moderate"
}
```

### Response
```json
{
  "summary": "Based on your symptoms of headache and fever...",
  "possibleConditions": ["Viral fever", "Common cold", "Migraine"],
  "recommendedSpecialties": [
    {
      "specialty": "General Medicine",
      "reason": "For initial evaluation of fever and headache",
      "priority": 1
    }
  ],
  "selfCareTips": ["Rest", "Stay hydrated", "Monitor temperature"],
  "urgencyLevel": "MEDIUM",
  "disclaimer": "This is not a medical diagnosis...",
  "followUpQuestions": "Do you have any other symptoms like..."
}
```

## Configuration Options

### Backend (`application.properties`)

| Property | Description | Default |
|----------|-------------|---------|
| `openai.api.key` | Your OpenAI API key | Required |
| `openai.api.url` | OpenAI API endpoint | https://api.openai.com/v1/chat/completions |
| `openai.model` | GPT model to use | gpt-4o-mini |
| `server.port` | Service port | 8085 |

### Environment Variables

| Variable | Description |
|----------|-------------|
| `OPENAI_API_KEY` | Your OpenAI API key |
| `VITE_AI_SERVICE_URL` | Frontend AI service URL |

## Important Notes

1. **Medical Disclaimer**: This service provides preliminary guidance only and is NOT a substitute for professional medical diagnosis or treatment.

2. **Emergency Situations**: For severe symptoms or emergencies, users should call emergency services (108 in India) or visit the nearest hospital immediately.

3. **API Key Security**: Never commit your OpenAI API key to version control. Use environment variables or secure secret management.

4. **Cost Considerations**: Each API call consumes OpenAI tokens. Monitor usage to manage costs.

## Integration with Doctor Search

The AI recommendations include direct links to find specialists in the doctor search page. When a user clicks "Find [Specialty]", they are redirected to the doctor search with the specialty pre-filled.

## Future Enhancements

- Conversation history persistence
- Multi-language support
- More detailed symptom questionnaires
- Integration with actual doctor availability
- Symptom severity tracking over time
