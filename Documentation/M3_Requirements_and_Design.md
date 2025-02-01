# M3 - Requirements and Design

## 1. Change History
<!-- Leave blank for M3 -->

## 2. Project Description
“Did you feed the animals?” “Are the animals fed?” “Anyone feed the animals?” These texts flood the family group chat every day. The kids don’t respond. Mom is still at work. No one knows if the beasts have been fed. The beasts also lie, as though they are starving and wasting away even if they already got dinner. Many task managing apps out there are WAY too complicated. TAS is straightforward app that indicates whether or not your beloved household pet has been fed. To go even further, there will be a minimal mode, for those living with grandparents, small children, or a grumpy father that does not want to be bothered with a new app. Simple, easy, convenient. 

## 3. Requirements Specification
### **3.1. Use-Case Diagram**


### **3.2. Actors Description**
1. **Household Manager**:
   - Most technically capable user in the household
   - Initiates the household
   - Will control the users, permissions, pets, and feeding schedule
   - Also has access to all of the below
2. **Household Member/Regular User**:
   -   Average household member that will be able to feed the pet.
   -   Can request that another regular user feed the pet
   -   Receives notifications and feeding requests
3. **Restricted User**:
   - Household member who struggles with technology and needs/wants a limited UI
   - Can submit to the app when they’ve fed the pet
   - Can check the app to see whether they need to feed the pet or not

### **3.3. Functional Requirements**
<a name="fr1"></a>

1. **Log Feeding** 
    - **Overview**:
        1. Users must be able to log that a pet has been fed, ensuring accurate feeding records for tracking purposes.

    - **Detailed Flow for Each Independent Scenario**: 
        1. **Log Feeding Event**:
            - **Description**: Users log a feeding event by selecting the pet they’ve fed and confirming the action.
            - **Primary actor(s)**: All household member
            - **Main success scenario**:
                1. User scrolls through the list of pets on the base page to find the pet being fed.
                2. User presses the corresponding “Feed Pet” button to confirm that the pet has been fed.
                3. System updates the feeding log with the pet's ID, user ID, date, and amount of food.
                4. User is prompted with a success message indicating that the log has been updated successfully.
            - **Failure scenario(s)**:
                - 1a. App is unable retrieve the log data. 
                    - 1a1. User is redirected back to the home page and system displays an error message prompting the user to try again later.
                - 1c. App is unable to update the system
                    - 1c1. User is redirected back to the pet's page after the failure and prompted that the logging was unsuccesful and should try again.
                    - 1c2. User presses the “Feed Pet” button again to attempt logging the feeding once more
    
2. **Requesting Others to do Feeding** 
    - **Overview**:
        1. Regular users must be able to notify other regular users to feed the pet. The receiving user must be notified that they are responsible for feeding the pet.

    - **Detailed Flow for Each Independent Scenario**: 
        1. **Feeding Request Notification**:
            - **Description**: Users can send a notification to another user, requesting them to feed the pet. The receiving user is notified of their responsibility to feed the pet. 
            - **Primary actor(s)**: Household Manager, Regular User
            - **Main success scenario**:
                1. Sender scrolls the main page to find the pet they want fed and selects the recipient in a droplist
                2. The sender then presses the “Notify” button
                3. App sends a request to the server to notify the recipient.
                4. User 2 (the recipient) receives a notification indicating that they are responsible for feeding the pet.
            - **Failure scenario(s)**:
                - 1c. App is unable to send the request to the server.
                    - 1c1. System displays and error message prompting the user to try again later
                    - 1c2. User dismisses the message and is sent back to the list of pets to try again
                    
3. **Managing The Household** 
    - **Overview**:
        1. Household Manager must be able to set up and edit the household, including managing members and pets, and setting feeding schedules.

    - **Detailed Flow for Each Independent Scenario**: 
        1. **Add Users**:
            - **Description**: Household manager can add users to the household
            - **Primary actor(s)**: Household Manager
            - **Main success scenario**:
                1. Manager clicks the "Manage Household" button on the top of the page and is directed to a page with the "Add User" button
                2. Manager is prompted to enter the userID of the person they are inviting
                3. Manger clicks "ok" 
                4. The server adds the user to the household 
                5. A toast message is displayed confirming that the user has been added
            - **Failure scenario(s)**:
                - 1d. The app is unable to update the server 
                    - 1d1. The app displays an error message and prompts the user with a message to try again
                    - 1d2. The input form is re-displayed, and the user types in the code again
                - 2d. The user does not exist
                    - 2d1. The app displays an error message saying that the user does not exist and should make sure they typed in the ID correct and the person has an account on the app
                    -2d2. The user dismisses the message and is redirected back to the manage household page
        2. **Manage Pets**:
            - **Description**: Household manager can add pets and change their feeding schedules
            - **Primary actor(s)**: Household manager
            - **Main success scenario**:
                1. Manager clicks the "Manage Household" button on the top of the page and is directed to a page with the "Manage Pets" button
                2. Manager clicks the "Add Pet" button or selects the edit icon on the pet they want to update, and the field becomes editable
                3. Manger clicks "ok" 
                4. The app updates the database with these changes
                5. A toast message is displayed confirming that the changes have been made
            - **Failure scenario(s)**:
                - 1a. 
                    - 1a1.
                    
4. **History Management** 
    - **Overview**:
        1. Household Manager must be able to view the history of logs, including log time and log member.

    - **Detailed Flow for Each Independent Scenario**: 
        1. **Manage History**:
            - **Description**: 
            - **Primary actor(s)**: 
            - **Main success scenario**:
                1. Household Manager clicks the “View History” button on the main screen
                2. The history is retrieved and the user is directed to a new screen displaying the feeding history in the household
            - **Failure scenario(s)**:
                - 1b. Server is unable to retrieve the history
                    - 1b1. App displays an error message saying the history could not be retrieved and the user should try again later
                    - 1b2. 
                    
5. **Feeding Time Notification** 
    - **Overview**:
        1. User must receive a notification if it is time to feed the pet and the pet has not been fed.

    - **Detailed Flow for Each Independent Scenario**: 
        1. **Feeding Notification**:
            - **Description**: The system sends a time-based notification to users when it’s time to feed the pet, ensuring that pets are fed on schedule.
            - **Primary actor(s)**: Household Manager, Regular Users 
            - **Main success scenario**:
                1. The feeding schedule for the pet is triggered based on the time set by the user.
                2. The system sends a push notification to the user indicating that it is time to feed the pet.
                3. User receives the push notification on their device
            - **Failure scenario(s)**:
                - 1a. 
                    - 1a1.
                    
6. **Login Authentication** 
    - **Overview**:
        1. Users must be able to log in using Google authentication.

    - **Detailed Flow for Each Independent Scenario**: 
        1. **Login/Signup**:
            - **Description**: Users access the in-app account via their google account to gain access to the app
            - **Primary actor(s)**: All users
            - **Main success scenario**:
                1. User clicks the "Login Button"
                2. User is directed to the google authentication page
                3. If the user has an account, their household information is retrieved and they are sent to the main screen. Otherwise, if the user does not have an account, they are added to the database and displayed a welcome message.
            - **Failure scenario(s)**:
                - 1b. User is unable to authenticate through Google.
                    - 1b1. The app encounters an issue during the authentication process (e.g., incorrect credentials, server error)
                    - 1b2. The system displays an error message prompting the user to try again. 
                    - 1b3. The user dismisses the message and is redirected to the login screen
                

<!--### **3.4. Screen Mockups**-->


### **3.5. Non-Functional Requirements**
<a name="nfr1"></a>

1. **[WRITE_NAME_HERE]**
    - **Description**: ...
    - **Justification**: ...
2. ...


## 4. Designs Specification
### **4.1. Main Components**
1. **[WRITE_NAME_HERE]**
    - **Purpose**: ...
    - **Interfaces**: 
        1. ...
            - **Purpose**: ...
        2. ...
2. ...


### **4.2. Databases**
1. **[WRITE_NAME_HERE]**
    - **Purpose**: ...
2. ...


### **4.3. External Modules**
1. **[WRITE_NAME_HERE]** 
    - **Purpose**: ...
2. ...


### **4.4. Frameworks**
1. **[WRITE_NAME_HERE]**
    - **Purpose**: ...
    - **Reason**: ...
2. ...


### **4.5. Dependencies Diagram**


### **4.6. Functional Requirements Sequence Diagram**
1. [**[WRITE_NAME_HERE]**](#fr1)\
[SEQUENCE_DIAGRAM_HERE]
2. ...


### **4.7. Non-Functional Requirements Design**
1. [**[WRITE_NAME_HERE]**](#nfr1)
    - **Validation**: ...
2. ...


### **4.8. Main Project Complexity Design**
**[WRITE_NAME_HERE]**
- **Description**: ...
- **Why complex?**: ...
- **Design**:
    - **Input**: ...
    - **Output**: ...
    - **Main computational logic**: ...
    - **Pseudo-code**: ...
        ```
        
        ```


## 5. Contributions
- ...
- ...
- ...
- ...
