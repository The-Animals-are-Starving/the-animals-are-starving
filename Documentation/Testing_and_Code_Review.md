# M5: Testing and Code Review

## 1. Change History

| **Change Date**   | **Modified Sections** | **Rationale** |
| ----------------- | --------------------- | ------------- |
| _Nothing to show_ |

---

## 2. Back-end Test Specification: APIs

### 2.1. Locations of Back-end Tests and Instructions to Run Them

#### 2.1.1. Tests

| **Interface**                 | **Describe Group Location, No Mocks**                | **Describe Group Location, With Mocks**            | **Mocked Components**              |
| ----------------------------- | ---------------------------------------------------- | -------------------------------------------------- | ---------------------------------- |
| **POST /user/**          | [`tests/mockFree/userRoutes.mockFree.test.ts#L34`](#) | [`tests/mocked/userRoutes.mockFed.test.ts#L16`](#) | HouseDB |
| **GET /user/:householdId** | [`tests/mockFree/userRoutes.mockFree.test.ts#L60`](#) | [`tests/mocked/userRoutes.mockFed.test.ts#L85`](#) | HouseDB |
| **GET /user/specific-user/:email** | [`tests/mockFree/userRoutes.mockFree.test.ts#L81`](#) | [`tests/mocked/userRoutes.mockFed.test.ts#L127`](#) | HouseDB                     |
| **PATCH /user/update-household/:email** | [`tests/mockFree/userRoutes.mockFree.test.ts#L104`](#) | [`tests/mocked/userRoutes.mockFed.test.ts#L171`](#) | HouseDB                     |
| **PATCH /user/:email** | [`tests/mockFree/userRoutes.mockFree.test.ts#L129`](#) | [`tests/mocked/userRoutes.mockFed.test.ts#L219`](#) | HouseDB                     |
| **DELETE /user/:email** | [`tests/mockFree/userRoutes.mockFree.test.ts#L166`](#) | [`tests/mocked/userRoutes.mockFed.test.ts#L279`](#) | HouseDB                     |
| **POST /household/create** | [`tests/mockFree/houseRoutes.mockFree.test.ts#L???`](#) | [`tests/mocked/houseRoutes.mockFed.test.ts#L19`](#) | HouseDB                     |
| **POST /pet/** | [`tests/mockFree/petRoutes.mockFree.test.ts#L???`](#) | [`tests/mocked/petRoutes.mockFed.test.ts#L88`](#) | HouseDB                     |
| **GET /pet/:householdId** | [`tests/mockFree/petRoutes.mockFree.test.ts#L???`](#) | [`tests/mocked/petRoutes.mockFed.test.ts#L141`](#) | HouseDB                     |
| **PATCH /pet/:petName/feed** | [`tests/mockFree/petRoutes.mockFree.test.ts#L???`](#) | [`tests/mocked/petRoutes.mockFed.test.ts#L174`](#) | HouseDB                     |
| **DELETE /pet/:petName** | [`tests/mockFree/petRoutes.mockFree.test.ts#L???`](#) | [`tests/mocked/petRoutes.mockFed.test.ts#L269`](#) | HouseDB                     |
| **POST /log/:petName** | [`tests/mockFree/logRoutes.mockFree.test.ts#L???`](#) | [`tests/mocked/logRoutes.mockFed.test.ts#L24`](#) | HouseDB                     |
| **GET /log/pet/:petId** | [`tests/mockFree/logRoutes.mockFree.test.ts#L???`](#) | [`tests/mocked/logRoutes.mockFed.test.ts#L101`](#) |HouseDB                     |
| **GET /log/household/:householdId** | [`tests/mockFree/logRoutes.mockFree.test.ts#L???`](#) | [`tests/mocked/logRoutes.mockFed.test.ts#L142`](#) |HouseDB                     |
| **GET /log/:user/:userEmail** | [`tests/mockFree/logRoutes.mockFree.test.ts#L???`](#) | [`tests/mocked/logRoutes.mockFed.test.ts#L174`](#) | HouseDB                     |
| **GET /analytics/rankings/:householdId** | [`tests/mockFree/analyticsRoutes.mockFree.test.ts#L32`](#) | [`tests/mocked/analyticsRoutes.mockFed.test.ts#L15`](#) | HouseDB                     |
| **POST /notify/:email** | [`tests/mockFree/notificationsRoutes.mockFree.test.ts#L38`](#) | [`tests/mocked/notificationsRoutes.mockFed.test.ts#L17`](#) | HouseDB, Firebase Messaging                     |

#### 2.1.2. Commit Hash Where Tests Run

`SEE LATEST COMMIT`
<mark>TO DO: FINALIZE FOR SUBMISSION</mark>

#### 2.1.3. Explanation on How to Run the Tests

1. **Clone the Repository**:

   - Open your terminal and run:
     ```
     git clone https://github.com/matthewfung04/the-animals-are-starving.git
     cd the-animals-are-starving/Backend
     ```

2. **Environment File for Running the Server**:
   - If you want to run the server locally, create a .env file with the following:
     ```
     DB_URI=mongodb://localhost:27017
     PORT=5000
     ```
   - If you wish to run the firebase notifications, you will need to create a key or get ours, but we recommend just using our server.  

3. **Install the relevant packages**:
   - First install the packages needed with the command below. This may take a while (multiple minutes) as some packages are large.
     ```
     npm install
     ```

4. **Running the Test(s)**
   - To simply run all the tests, use:
    
     ```
     npm test
     ```
   - To run with coverage, use:
     ```
     npm run test:coverage
     ```
   - To run a specific folder (i.e., mocked or mockFree) or singular file. Just add the path to either command as follows:
    
     ```
     npm test [path]
     ```

### 2.2. GitHub Actions Configuration Location

`~/.github/workflows/jest-tests.yml`

### 2.3. Jest Coverage Report Screenshots With Mocks

![image](https://github.com/user-attachments/assets/f5e17ce0-e058-47ff-aec0-b6190b1ea92c)

### 2.4. Jest Coverage Report Screenshots Without Mocks

_(Placeholder for Jest coverage screenshot without mocks)_ <mark>TO DO: Make it...</mark>

### 2.5 Missing Coverage Justification (Without Mocks)

#### Analytics Routes
- (21-22) An error in the route which we'd need a mock to test
- (74-75) A catch block from a database error, again requires a mock

#### Log Routes

#### Notifications Routes

#### UserHousehold Routes

#### User Routes
- (25, 37, 54, 80, 99, 117) All catch blocks which require a mock to test

---

## 3. Back-end Test Specification: Tests of Non-Functional Requirements

### 3.1. Test Locations in Git

| **Non-Functional Requirement**  | **Location in Git**                              |
| ------------------------------- | ------------------------------------------------ |
| **Performance (Response Time)** | [`tests/non-functional/performance.test.js`](#) |

### 3.2. Test Verification and Logs

- **Performance (Response Time)**

  - **Verification:** This test suite simulates multiple concurrent API calls using Jest along with a load-testing utility to mimic real-world user behavior. The focus is on key endpoints such as retrieving user and pet data as well as household creation to ensure that each call completes within the target response time of 2 seconds under the worst loads for an app of our size (100 concurrent calls). The test logs capture metrics such as average response time, maximum response time, and error rates. These logs are then analyzed to identify any performance bottlenecks, ensuring the system can handle expected traffic without degradation in user experience.
  - **Log Output**
    <pre>
    <span style="background-color: green; color: white;">PASS</span> tests/non-functional/performance.test.ts (6.24 s)
    Performance Non-Functional Tests
    √ should respond quickly for concurrent calls to user endpoints (417 ms)
    √ should respond quickly for concurrent calls to pet endpoints (233 ms)                 
    √ should respond quickly for concurrent calls to household endpoints (344 ms)
    Test Suites: 1 passed, 1 total
    Tests:       3 passed, 3 total
    Snapshots:   0 total
    Time:        6.358 s, estimated 7 s
    </pre>

  - Note: our second non-functional requirement was tested on the front-end

---

## 4. Front-end Test Specification

### 4.1. Location in Git of Front-end Test Suite:

`src/app/src/androidTest/java/com/example/theanimalsarestarving/EspressoTest.java`

#### How To Run Tests
 - Make sure the backend is running with `npm run dev` in the `backend` folder
 - Enter a `mongosh` shell in the `backend` folder
 - Type `use pet-tracker` and then `load("initdb.mongo")`

### 4.2. Tests

- **Use Case: Log Feeding (Test Success)**

  - **Expected Behaviors:**
    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | 1. User scrolls through the list of pets on the base page to find the pet being fed. | Check that the feed button is present and click it.<br>Locate the desired pet on the page and ensure it exists. |
    | 2. User presses the corresponding “Feed Pet” button to confirm that the pet has been fed. | Locate the feed button with accociated pet and press it. |
    | 3. System updates the feeding log with the pet's ID, user ID, date, and amount of food. | Exit feeding menu.<br>Checks log button exists.<br>Clicks log button.<br>Checks that new log appeared. |
    | 4. User is prompted with a success message indicating that the log has been updated successfully. | While on feed screen, check clicked pet for "FED" text. |
    
    No failure scenarios as they all relate to server issues which requires mocking.

- **Use Case: Requesting Others to do Feeding (Test Success)**

  - **Expected Behaviors:**
    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | 1. Sender begins at the home page and presses "Notify Other Users". | Launch MainActivity and click notiy_button|
    | 2. The sender then presses the “Notify” button next to the name of the recipient. | First check that the a row with "Bob" appears with a "Notify" button. |
    | 3. App sends a request to the server to notify the recipient. | Verify that the button is pressable by clicking "Nofity" |
    | 4. User 2 (the recipient) receives a notification indicating that they are responsible for feeding the pet. | This cannot be tested explicitly as we only have one device |

     No failure scenarios as they all relate to server issues which requires mocking.

- **Use Case: History Management (Test Success)**

  - **Expected Behaviors:**
    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | 1. Household Manager clicks the “View History” button on the main screen | Check that the log button is present and click it. |
    | 2. The history is retrieved and the user is directed to a new screen displaying the feeding history in the household | Check to see if existing logs in the backend appear on screen. |

- **Use Case: History Management (Test Failure)**

  - **Expected Behaviors:**
    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | 1. Household Manager clicks the “View History” button on the main screen<br> - 1a. User is not a Household Manager<br> - 1a1. User is not prompted with the "View History" button | Set current user to a non manager user.<br>Check that the view log button does not exist.<br>Set current user back to manager user. |

- **Test Logs:**
  ```
  com.example.theanimalsarestarving.EspressoTest   26.92 s
  passed testLogFeedingUseCase   10.59 s 
  passed testNotifications    3.92 s
  passed testHistoryManagementUseCaseSuccess   2.85 s
  passed testHistoryManagementUseCaseFailure   1.24 s
  ```


### 4.3. Front-end Non-Functional Test

| **Non-Functional Requirement**  | **Location in Git**                              |
| ------------------------------- | ------------------------------------------------ |
| **Accessability (# of Clicks)** | src/app/src/androidTest/java/com/example/theanimalsarestarving/EspressoTest.java|

#### 4.3.1. Test Verification and Logs

- **Accessability (# of Clicks)**

  - **Verification:** This test suite simulates a how a real-world user would access the use-case with the longest sequence of clicks. The focus is on making sure that the app should be usable by all members of the household, including those with impaired vision, language barriers, and of all mental faculties. Thus, users should be able to complete the action in 3 clicks or less. We did this by running tests starting from the home screen, and ensuring that we can feed a pet and view history by using less than three perform(click()) calls.
  - **Log Output**
    ```
    com.example.theanimalsarestarving.EspressoTest   26.92 s
    passed threeClickTest   8.32 s
    ```

---

## 5. Automated Code Review Results

### 5.1. Commit Hash Where Codacy Ran

`[Insert Commit SHA here]` `SEE LATEST COMMIT`
<mark>TO DO: FINALIZE FOR SUBMISSION</mark>

### 5.2. Unfixed Issues per Codacy Category

![image](https://github.com/user-attachments/assets/30c352c1-8e4e-4041-bb7f-1cdcc12b0be6)


### 5.3. Unfixed Issues per Codacy Code Pattern

![image](https://github.com/user-attachments/assets/384e2d52-a8be-4496-b00f-d670cd8f6659)

![image](https://github.com/user-attachments/assets/bcf80270-eac1-4f23-9535-656ceb1d455a)

![image](https://github.com/user-attachments/assets/946886b3-741f-4199-911c-a305dca659b3)


### 5.4. Justifications for Unfixed Issues

- **Code Pattern: [Expression with labels increase complexity and affect maintainability.](#)**

  1. **Expression with labels increase complexity and affect maintainability.**

     - **Location in Git:** [`src/app/src/main/java/com/example/theanimalsarestarving/activities/MainActivity.kt#L99`](#)
     - **Location in Git:** [`src/app/src/main/java/com/example/theanimalsarestarving/activities/MainActivity.kt#L150`](#)
     - **Location in Git:** [`src/app/src/main/java/com/example/theanimalsarestarving/activities/MainActivity.kt#L106`](#)
     - **Justification:** Labels can reduce code redundancy by preventing duplicated logic. Kotlin provides labels as an intentional feature to handle nested control flow; for instance, to simulate a break from a higher-level loop or to return from a lambda, you can label the loop or lambda and use a return at that label. The official Kotlin docs even demonstrate that there’s “no direct equivalent for break, but it can be simulated by adding another nesting lambda and non-locally returning from it”. ([Kotlin Docs](https://kotlinlang.org/docs/returns.html#:~:text=There%20is%20no%20direct%20equivalent,locally%20returning%20from%20it)) In our case, these breaks were necessary, as they are part of onCreate, and reordering is not possible during setup.

- **Code Pattern: [Too many functions inside a/an file/class/object/interface.](#)**

  1. **Class 'ManageHouseholdActivity' with '12' functions detected.**

     - **Location in Git:** [`src/app/src/main/java/com/example/theanimalsarestarving/activities/ManageHouseholdActivity.kt#L36`](#)
     - **Justification:** Having a dozen functions inside ManageHouseholdActivity might superficially trigger the “large class” warning, but it can still be reasonable to keep them together. Generally, a class growing too large can hint at too many responsibilities. In this case, though, all 12 functions belong to one feature area – managing a household – which means the class still has a singular, focused responsibility. A class can have multiple methods yet remain cohesive if those methods all serve a unified purpose. ManageHouseholdActivity encapsulates various actions (adding/removing pets and users, etc.) but they all pertain to the household management feature.
     
        While some static analysis tools warn against classes with many methods, a high number of functions does not automatically indicate poor design. In fact, industry best practices—as outlined in [Clean Code: A Handbook of Agile Software Craftsmanship by Robert C. Martin (2008)](https://www.amazon.ca/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882)—stress that each class should have a single responsibility, grouping the proper ideas together. Our ManageHouseholdActivity groups 12 methods that all work toward the same cohesive purpose: managing household data and behavior. Thus, the number of functions is justified by their close interrelation and does not compromise maintainability or clarity.
     
        Furthermore, the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) do not impose an arbitrary maximum on the number of functions per file or class—only that they are organized in a logical, readable manner. In our case, keeping 12 functions within the ManageHouseholdActivity ensures all related behavior is encapsulated in one place.
  2. **Interface 'ApiService' with '17' functions detected'**

     - **Location in Git:** [`src/app/src/main/java/com/example/theanimalsarestarving/network/ApiService.kt#L16`](#)
     - **Justification:** For similar reasons as above, we have kept these functions together. Moreover, this is a file that is used as a list of all the exposed routes to the back-end, so it doesn't make sense to split up the file just to remove the flag. Keeping the routes together makes it simpler to reference the needed routes and avoids the extra confusion of having separate files for routes.
     
- **Code Pattern: [Function is too long.](#)**

  1. **onCreate is too long**

     - **Location in Git:** [`src/app/src/main/java/com/example/theanimalsarestarving/activities/MainActivity.kt#L71`](#)
     - **Justification:** Given that onCreate() is a one-time setup method where all initialization tasks are tightly coupled and must occur in a specific order, the benefits of maintaining a cohesive, single-location initialization sequence outweigh the risks associated with its length. Refactoring into smaller methods could lead to fragmented logic and decreased clarity. This trade-off has been carefully considered and aligns with Android’s official guidelines and widely accepted best practices ([Android Developer Documentation](https://developer.android.com/guide/components/activities/activity-lifecycle)).
