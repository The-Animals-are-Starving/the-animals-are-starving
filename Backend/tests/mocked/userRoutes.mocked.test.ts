import request from "supertest";
import express from "express";
import router from "../../routes/userRoutes";
import User from "../../models/User";

// Mocks are used for database operations.
jest.mock("../../models/User");

const app = express();
app.use(express.json());
app.use("/", router);

//
// POST / (CreateUser) – Unit Test (with mocks)
//
describe("POST / - CreateUser (Mocked)", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  // Test: Create a new user successfully when no existing user is found.
  // Input: { email, name, householdId }.
  // Expected: HTTP 201, message "User created successfully", and user object with provided email.
  it("should create a new user if one does not exist", async () => {
    const userData = { email: "test@example.com", name: "Test User", householdId: "house123" };

    // Mock: simulate no existing user in DB.
    (User.findOne as jest.Mock).mockResolvedValue(null);
    // Mock: simulate saving the user and returning the data.
    const saveMock = jest.fn().mockResolvedValue(userData);
    (User as unknown as jest.Mock).mockImplementation((data) => ({
      ...data,
      save: saveMock,
    }));

    const res = await request(app).post("/").send(userData);
    expect(res.status).toBe(201);
    expect(res.body.message).toBe("User created successfully");
    expect(res.body.user.email).toBe(userData.email);
    expect(User.findOne).toHaveBeenCalledWith({ email: userData.email });
    expect(saveMock).toHaveBeenCalled();
  });

  // Test: Return 400 if the user already exists.
  // Input: Same user data that exists in DB.
  // Expected: HTTP 400 and message "User already exists".
  it("should return 400 if the user already exists", async () => {
    const userData = { email: "test@example.com", name: "Test User", householdId: "house123" };
    (User.findOne as jest.Mock).mockResolvedValue(userData);

    const res = await request(app).post("/").send(userData);
    expect(res.status).toBe(400);
    expect(res.body.message).toBe("User already exists");
  });

  it("should return 500 if an error occurs during user creation (findOne error)", async () => {
    const userData = { email: "error@example.com", name: "Error User", householdId: "house123" };
    (User.findOne as jest.Mock).mockRejectedValue(new Error("findOne error"));

    const res = await request(app).post("/").send(userData);
    expect(res.status).toBe(500);
    expect(res.body.message).toBe("Error creating user");
    expect(res.body.error).toBeDefined();
  });

  it("should return 500 if an error occurs during user creation (save error)", async () => {
    const userData = { email: "error2@example.com", name: "Error User", householdId: "house123" };
    (User.findOne as jest.Mock).mockResolvedValue(null);
    const saveMock = jest.fn().mockRejectedValue(new Error("save error"));
    (User as unknown as jest.Mock).mockImplementation((data) => ({
      ...data,
      save: saveMock,
    }));

    const res = await request(app).post("/").send(userData);
    expect(res.status).toBe(500);
    expect(res.body.message).toBe("Error creating user");
    expect(res.body.error).toBeDefined();
  });
});

//
// GET /:householdId (GetAllUsers) – Unit Test (with mocks)
//
describe("GET /:householdId - GetAllUsers (Mocked)", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  // Test: Return a sorted list of users for the given household.
  // Input: householdId in URL parameter.
  // Expected: HTTP 200 and an array of user objects.
  it("should return a list of users for a household", async () => {
    const householdId = "house123";
    const users = [
      { name: "Alice", householdId },
      { name: "Bob", householdId },
    ];
    // Mock: simulate a query chain with sort() that returns the users.
    (User.find as jest.Mock).mockReturnValue({
      sort: jest.fn().mockResolvedValue(users),
    });

    const res = await request(app).get(`/${householdId}`);
    expect(res.status).toBe(200);
    expect(res.body).toEqual(users);
    expect(User.find).toHaveBeenCalledWith({ householdId });
  });

  // New test: simulate error in getAllUsers
  it("should return 500 if an error occurs retrieving users", async () => {
    const householdId = "house123";
    (User.find as jest.Mock).mockReturnValue({
      sort: jest.fn().mockRejectedValue(new Error("sort error")),
    });

    const res = await request(app).get(`/${householdId}`);
    expect(res.status).toBe(500);
    expect(res.body.message).toBe("Error retrieving users");
    expect(res.body.error).toBeDefined();
  });
});

//
// GET /specific-user/:email (GetUser) – Unit Test (with mocks)
//
describe("GET /specific-user/:email - GetUser (Mocked)", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  // Test: Return user details when a valid email is provided.
  // Input: email in URL parameter.
  // Expected: HTTP 200 and the user object.
  it("should return user details if the user exists", async () => {
    const email = "test@example.com";
    const user = { email, name: "Test User", householdId: "house123" };
    (User.findOne as jest.Mock).mockResolvedValue(user);

    const res = await request(app).get(`/specific-user/${email}`);
    expect(res.status).toBe(200);
    expect(res.body).toEqual(user);
  });

  // Test: Return 404 when the user is not found.
  // Input: non-existent email.
  // Expected: HTTP 404 and message "User not found".
  it("should return 404 if the user is not found", async () => {
    const email = "notfound@example.com";
    (User.findOne as jest.Mock).mockResolvedValue(null);

    const res = await request(app).get(`/specific-user/${email}`);
    expect(res.status).toBe(404);
    expect(res.body.message).toBe("User not found");
  });

  it("should return 500 if an error occurs retrieving the user", async () => {
    const email = "error@example.com";
    (User.findOne as jest.Mock).mockRejectedValue(new Error("findOne error"));

    const res = await request(app).get(`/specific-user/${email}`);
    expect(res.status).toBe(500);
    expect(res.body.message).toBe("Error retrieving user");
    expect(res.body.error).toBeDefined();
  });
});

//
// PATCH /update-household/:email (UpdateUserHouseholdId) – Unit Test (with mocks)
//
describe("PATCH /update-household/:email - UpdateUserHouseholdId (Mocked)", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  // Test: Successfully update the householdId for an existing user.
  // Input: email in URL parameter and new householdId in the request body.
  // Expected: HTTP 200, message "User Household ID updated successfully", and updated user object.
  it("should update the householdId if the user exists", async () => {
    const email = "test@example.com";
    const updates = { householdId: "newHouseId" };
    const updatedUser = { email, householdId: "newHouseId", name: "Test User" };
    (User.findOneAndUpdate as jest.Mock).mockResolvedValue(updatedUser);

    const res = await request(app).patch(`/update-household/${email}`).send(updates);
    expect(res.status).toBe(200);
    expect(res.body.message).toBe("User Household ID updated successfully");
    expect(res.body.user).toEqual(updatedUser);
  });

  // Test: Return 404 when updating householdId for a non-existent user.
  // Input: non-existent email.
  // Expected: HTTP 404 and message "User not found".
  it("should return 404 if the user is not found", async () => {
    const email = "notfound@example.com";
    const updates = { householdId: "newHouseId" };
    (User.findOneAndUpdate as jest.Mock).mockResolvedValue(null);

    const res = await request(app).patch(`/update-household/${email}`).send(updates);
    expect(res.status).toBe(404);
    expect(res.body.message).toBe("User not found");
  });

  it("should return 500 if an error occurs updating the user's household ID", async () => {
    const email = "error@example.com";
    const updates = { householdId: "newHouseId" };
    (User.findOneAndUpdate as jest.Mock).mockRejectedValue(new Error("update error"));

    const res = await request(app).patch(`/update-household/${email}`).send(updates);
    expect(res.status).toBe(500);
    expect(res.body.message).toBe("Error updating user household ID");
    expect(res.body.error).toBeDefined();
  });
});

//
// PATCH /:email (UpdateUserRole) – Unit Test (with mocks)
//
describe("PATCH /:email - UpdateUserRole (Mocked)", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  // Test: Successfully update the user role when a valid role is provided.
  // Input: email in URL parameter and { role: "manager" } in request body.
  // Expected: HTTP 200, message "User updated successfully", and updated user object with the new role.
  it("should update the user role if a valid role is provided", async () => {
    const email = "test@example.com";
    const updates = { role: "manager" };
    const updatedUser = { email, role: "manager", name: "Test User", householdId: "house123" };
    (User.findOneAndUpdate as jest.Mock).mockResolvedValue(updatedUser);

    const res = await request(app).patch(`/${email}`).send(updates);
    expect(res.status).toBe(200);
    expect(res.body.message).toBe("User updated successfully");
    expect(res.body.user).toEqual(updatedUser);
  });

  // Test: Return 400 when an invalid role is provided.
  // Input: email in URL parameter and { role: "invalid" } in request body.
  // Expected: HTTP 400 and message "Invalid role provided".
  it("should return 400 if an invalid role is provided", async () => {
    const email = "test@example.com";
    const updates = { role: "invalid" };

    const res = await request(app).patch(`/${email}`).send(updates);
    expect(res.status).toBe(400);
    expect(res.body.message).toBe("Invalid role provided");
  });

  // Test: Return 404 when attempting to update role for a non-existent user.
  // Input: non-existent email with valid role update.
  // Expected: HTTP 404 and message "User not found".
  it("should return 404 if the user is not found", async () => {
    const email = "notfound@example.com";
    const updates = { role: "manager" };
    (User.findOneAndUpdate as jest.Mock).mockResolvedValue(null);

    const res = await request(app).patch(`/${email}`).send(updates);
    expect(res.status).toBe(404);
    expect(res.body.message).toBe("User not found");
  });

  it("should return 500 if an error occurs updating the user role", async () => {
    const email = "error@example.com";
    const updates = { role: "manager" };
    (User.findOneAndUpdate as jest.Mock).mockRejectedValue(new Error("update error"));

    const res = await request(app).patch(`/${email}`).send(updates);
    expect(res.status).toBe(500);
    expect(res.body.message).toBe("Error updating user");
    expect(res.body.error).toBeDefined();
  });
});

//
// DELETE /:email (DeleteUser) – Unit Test (with mocks)
//
describe("DELETE /:email - DeleteUser (Mocked)", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  // Test: Successfully delete an existing user.
  // Input: email in URL parameter.
  // Expected: HTTP 200 and message "User deleted successfully".
  it("should delete the user if they exist", async () => {
    const email = "test@example.com";
    const user = { email, name: "Test User" };
    (User.findOneAndDelete as jest.Mock).mockResolvedValue(user);

    const res = await request(app).delete(`/${email}`);
    expect(res.status).toBe(200);
    expect(res.body.message).toBe("User deleted successfully");
  });

  // Test: Return 404 when trying to delete a non-existent user.
  // Input: non-existent email.
  // Expected: HTTP 404 and message "User not found".
  it("should return 404 if the user is not found", async () => {
    const email = "notfound@example.com";
    (User.findOneAndDelete as jest.Mock).mockResolvedValue(null);

    const res = await request(app).delete(`/${email}`);
    expect(res.status).toBe(404);
    expect(res.body.message).toBe("User not found");
  });

  it("should return 500 if an error occurs deleting the user", async () => {
    const email = "error@example.com";
    (User.findOneAndDelete as jest.Mock).mockRejectedValue(new Error("delete error"));

    const res = await request(app).delete(`/${email}`);
    expect(res.status).toBe(500);
    expect(res.body.message).toBe("Error deleting user");
    expect(res.body.error).toBeDefined();
  });
});