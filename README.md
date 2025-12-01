# 📱 SmartAir – Final Project  
### CSCB07: Software Design  
### Group 31 – Fall 2025  
#### Team Members:
- Jason Tam
- Jason Villablanca
- Sylvia Xu
- Rabiah Islam



## 🧠 Overview
SMART AIR is a kid-friendly Android app that helps children (ages 6–16) understand  asthma, practice good inhaler technique, log symptoms/medicine use, and share parent-approved information with a healthcare provider via a concise, exportable report.

This project was built as part of the CSCB07 final course project.


## ⭐ Key Features

### **Child Features**
- Daily symptom check-in (night waking, activity limits, cough/wheeze)
- Manual PEF entry with automatic zone classification (Green / Yellow / Red)
- Medication logging (controller + rescue)
- Triage questionnaire with severity detection and automatic parent alerts
- View streaks and badges (controller streaks, technique streaks, low-rescue month)
- View personal history of PEF readings, medicine logs, and daily check-ins

### **Parent Features**
- Full child dashboard (today's zone, last rescue time, weekly rescue counts, trend snippet)
- Add/manage multiple children (username, password, name, DOB/age, planned controller schedule, badge thresholds, and optional notes)
- Add providers per child (generate invite code and manage data sharing)
- Manage inhaler inventory (expiry, low amount, purchase date)
- Real-time alerts (red zone, triage escalation, low/expired inventory)
- View full history browser  with filters and export options for PDF or CSV  
- Generate shareable provider reports as PDF

### **Provider Features**
Full read-only role - can only access the following features if Parent has enabled sharing
- Controller adherence summary
- History of triage incidents 
- History of symptoms and triggers
- History of Peak-flow (PEF)
- History of rescue logs 


## 🛠 How to use

1. Clone the repository:
   ```bash
   git clone <repo-url>
2. Open the project in Android Studio
3. Allow Gradle to sync
4. Add Firebase config file:
   Place google-services.json into:
   ```bash
   app/google-services.json
5. Connect an emulator or physical Android device
6. Press Run inside Android Studio

## 📄 License
This project was created for academic purposes only as part of CSCB07 at the University of Toronto.
Not intended for commercial or medical use.

