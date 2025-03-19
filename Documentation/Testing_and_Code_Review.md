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
| **POST /household/create** | ...                                                  | ...                                                | HouseDB                     |
| **POST /househld/add-user** | ...                                                  | ...                                                | HouseDB                     |
| **POST /household/remove-user** | ...                                                  | ...                                                | HouseDB                     |
| **POST /pet/** | ...                                                  | ...                                                | HouseDB                     |
| **GET /pet/:householdId** | ...                                                  | ...                                                | HouseDB                     |
| **PATCH /pet/:petName/feed** | ...                                                  | ...                                                | HouseDB                     |
| **DELETE /pet/:petName** | ...                                                  | ...                                                | HouseDB                     |
| **POST /log/:petName** | ...                                                  | ...                                                | HouseDB                     |
| **GET /log/pet/:petId** | ...                                                  | ...                                                | HouseDB                     |
| **GET /log/household/:householdId** | ...                                                  | ...                                                | HouseDB                     |
| **GET /log/:user/:userEmail** | ...                                                  | ...                                                | HouseDB                     |
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
     ```

2. **Enter the Back-end Directory**:
   - Use:
     ```
     cd the-animals-are-starving/Backend
     ```

3. **Enter the Back-end Directory**:
   - First install the packages needed with the command below. This may take a while (multiple minutes) as some packages are large.
     ```
     cd the-animals-are-starving/Backend
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

_(Placeholder for Jest coverage screenshot with mocks enabled)_ <mark>TO DO: Make it...</mark>

### 2.4. Jest Coverage Report Screenshots Without Mocks

_(Placeholder for Jest coverage screenshot without mocks)_ <mark>TO DO: Make it...</mark>

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

`frontend/src/androidTest/java/com/studygroupfinder/`

### 4.2. Tests

- **Use Case: Login**

  - **Expected Behaviors:**
    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | 1. The user opens â€œAdd Todo Itemsâ€ screen. | Open â€œAdd Todo Itemsâ€ screen. |
    | 2. The app shows an input text field and an â€œAddâ€ button. The add button is disabled. | Check that the text field is present on screen.<br>Check that the button labelled â€œAddâ€ is present on screen.<br>Check that the â€œAddâ€ button is disabled. |
    | 3a. The user inputs an ill-formatted string. | Input â€œ_^_^^OQ#$â€ in the text field. |
    | 3a1. The app displays an error message prompting the user for the expected format. | Check that a dialog is opened with the text: â€œPlease use only alphanumeric charactersâ€. |
    | 3. The user inputs a new item for the list and the add button becomes enabled. | Input â€œbuy milkâ€ in the text field.<br>Check that the button labelled â€œaddâ€ is enabled. |
    | 4. The user presses the â€œAddâ€ button. | Click the button labelled â€œaddâ€. |
    | 5. The screen refreshes and the new item is at the bottom of the todo list. | Check that a text box with the text â€œbuy milkâ€ is present on screen.<br>Input â€œbuy chocolateâ€ in the text field.<br>Click the button labelled â€œaddâ€.<br>Check that two text boxes are present on the screen with â€œbuy milkâ€ on top and â€œbuy chocolateâ€ at the bottom. |
    | 5a. The list exceeds the maximum todo-list size. | Repeat steps 3 to 5 ten times.<br>Check that a dialog is opened with the text: â€œYou have too many items, try completing one firstâ€. |

  - **Test Logs:**
    ```
    [Placeholder for Espresso test execution logs]
    ```

- **Use Case: ...**

  - **Expected Behaviors:**

    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | ...                | ...                 |

  - **Test Logs:**
    ```
    [Placeholder for Espresso test execution logs]
    ```

- **...**

### 4.3. Front-end Non-Functional Test

| **Non-Functional Requirement**  | **Location in Git**                              |
| ------------------------------- | ------------------------------------------------ |
| **Accessability (# of Clicks)** | [`where?`](#) <mark>TO DO: Make it...</mark>|

### 3.2. Test Verification and Logs

- **Accessability (# of Clicks)**

  - **Verification:** This test suite simulates a how a real-world user would access the use-case with the longest sequence of clicks. The focus is on making sure that the app should be usable by all members of the household, including those with impaired vision, language barriers, and of all mental faculties. Thus, users should be able to complete the action in <mark>TO DO: NUMBER OF CLICKS</mark> clicks or less.
  - **Log Output**

---

## 5. Automated Code Review Results

### 5.1. Commit Hash Where Codacy Ran

`[Insert Commit SHA here]`

### 5.2. Unfixed Issues per Codacy Category

_(Placeholder for screenshots of Codacyâ€™s Category Breakdown table in Overview)_

### 5.3. Unfixed Issues per Codacy Code Pattern

_(Placeholder for screenshots of Codacyâ€™s Issues page)_

### 5.4. Justifications for Unfixed Issues

- **Code Pattern: [Usage of Deprecated Modules](#)**

  1. **Issue**

     - **Location in Git:** [`src/services/chatService.js#L31`](#)
     - **Justification:** ...

  2. ...

- ...
