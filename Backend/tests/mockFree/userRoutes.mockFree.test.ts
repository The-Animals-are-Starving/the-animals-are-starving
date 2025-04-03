import request from 'supertest';
import express from 'express';
import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import router from '../../routes/userRoutes';
import User from '../../models/User';

const app = express();
app.use(express.json());
app.use("/", router);

let mongoServer: MongoMemoryServer;

beforeAll(async () => {
  // Set up in-memory MongoDB
  mongoServer = await MongoMemoryServer.create();
  const uri = mongoServer.getUri();
  await mongoose.connect(uri);
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

afterEach(async () => {
  // Clean up database between tests
  await User.deleteMany({});
});

//
// POST / (CreateUser) – (Without Mocks)
//
describe("POST / - CreateUser (without mocks)", () => {
  it("should create a new user if one does not exist", async () => {
    // Input: valid user data { email, name, householdId }
    // Expected: HTTP 201, message "User created successfully", and user object with given email
    const userData = { email: "test@example.com", name: "Test User", householdId: "house123" };
    const res = await request(app).post("/").send(userData);
    expect(res.status).toBe(201);
    expect(res.body.message).toBe("User created successfully");
    expect(res.body.user.email).toBe(userData.email);

    // Verify user exists in DB
    const userInDb = await User.findOne({ email: userData.email });
    expect(userInDb).not.toBeNull();
    expect(userInDb!.householdId).toBe("house123");
});

  it("should return 400 if the user already exists", async () => {
    // Setup: Insert a user directly into the database.
    const userData = { email: "test@example.com", name: "Test User", householdId: "house123" };
    await new User(userData).save();

    // Input: same user data; Expected: HTTP 400 and error message "User already exists"
    const res = await request(app).post("/").send(userData);
    expect(res.status).toBe(400);
    expect(res.body.message).toBe("User already exists");
  });
});

//
// GET /:householdId (GetAllUsers) – (Without Mocks)
//
describe("GET /:householdId - GetAllUsers (without mocks)", () => {
  it("should return a list of users for the given household", async () => {
    // Setup: Insert two users with the same householdId.
    const householdId = "house123";
    const usersData = [
      { email: "a@example.com", name: "Alice", householdId },
      { email: "b@example.com", name: "Bob", householdId }
    ];
    await User.insertMany(usersData);

    // Input: householdId as URL parameter; Expected: HTTP 200 and an array sorted by name.
    const res = await request(app).get(`/${householdId}`);
    expect(res.status).toBe(200);
    expect(res.body.length).toBe(2);
    expect(res.body[0].name).toBe("Alice");
  });
});

//
// GET /specific-user/:email (GetUser) – (Without Mocks)
//
describe("GET /specific-user/:email - GetUser (without mocks)", () => {
  it("should return user details if user exists", async () => {
    // Setup: Insert a user.
    const userData = { email: "test@example.com", name: "Test User", householdId: "house123" };
    await new User(userData).save();

    // Input: user email in URL parameter; Expected: HTTP 200 and the user object.
    const res = await request(app).get(`/specific-user/${userData.email}`);
    expect(res.status).toBe(200);
    expect(res.body.email).toBe(userData.email);
  });

  it("should return 404 if user does not exist", async () => {
    // Input: non-existent email; Expected: HTTP 404 and message "User not found".
    const res = await request(app).get(`/specific-user/notfound@example.com`);
    expect(res.status).toBe(404);
    expect(res.body.message).toBe("User not found");
  });
});

//
// PATCH /update-household/:email (UpdateUserHouseholdId) – (Without Mocks)
//
describe("PATCH /update-household/:email - UpdateUserHouseholdId (without mocks)", () => {
  it("should update the householdId if user exists", async () => {
    // Setup: Insert a user.
    const userData = { email: "test@example.com", name: "Test User", householdId: "house123" };
    await new User(userData).save();

    // Input: new householdId in request body; Expected: HTTP 200 and updated user object.
    const updates = { householdId: "newHouseId" };
    const res = await request(app).patch(`/update-household/${userData.email}`).send(updates);
    expect(res.status).toBe(200);
    expect(res.body.message).toBe("User Household ID updated successfully");
    expect(res.body.user.householdId).toBe("newHouseId");

    // Verify update in DB
    const updatedUser = await User.findOne({ email: userData.email });
    expect(updatedUser!.householdId).toBe("newHouseId");
  });

  it("should return 404 if user does not exist", async () => {
    // Input: non-existent email and update payload; Expected: HTTP 404 and message "User not found".
    const res = await request(app).patch(`/update-household/notfound@example.com`).send({ householdId: "newHouseId" });
    expect(res.status).toBe(404);
    expect(res.body.message).toBe("User not found");

  });
});

//
// PATCH /:email (UpdateUserRole) – (Without Mocks)
//
describe("PATCH /:email - UpdateUserRole (without mocks)", () => {
  it("should update the user role if valid role is provided", async () => {
    // Setup: Insert a user with role "normal".
    const userData = { email: "test@example.com", name: "Test User", householdId: "house123", role: "normal" };
    await new User(userData).save();

    // Input: valid role update { role: "manager" }; Expected: HTTP 200 and updated role.
    const updates = { role: "manager" };
    const res = await request(app).patch(`/${userData.email}`).send(updates);
    expect(res.status).toBe(200);
    expect(res.body.message).toBe("User updated successfully");
    expect(res.body.user.role).toBe("manager");
  });

  it("should return 400 if an invalid role is provided", async () => {
    // Setup: Insert a user.
    const userData = { email: "test@example.com", name: "Test User", householdId: "house123", role: "normal" };
    await new User(userData).save();

    // Input: invalid role { role: "invalid" }; Expected: HTTP 400 and message "Invalid role provided".
    const updates = { role: "invalid" };
    const res = await request(app).patch(`/${userData.email}`).send(updates);
    expect(res.status).toBe(400);
    expect(res.body.message).toBe("Invalid role provided");
  });

  it("should return 404 if user does not exist", async () => {
    // Input: non-existent email with valid role update; Expected: HTTP 404 and message "User not found".
    const res = await request(app).patch(`/notfound@example.com`).send({ role: "manager" });
    expect(res.status).toBe(404);
    expect(res.body.message).toBe("User not found");
  });
});

//
// DELETE /:email (DeleteUser) – (Without Mocks)
//
describe("DELETE /:email - DeleteUser (without mocks)", () => {
  it("should delete the user if they exist", async () => {
    // Setup: Insert a user.
    const userData = { email: "test@example.com", name: "Test User", householdId: "house123" };
    await new User(userData).save();

    // Input: user email in URL parameter; Expected: HTTP 200 and message "User deleted successfully".
    const res = await request(app).delete(`/${userData.email}`);
    expect(res.status).toBe(200);
    expect(res.body).toBe(true);

    // Verify user is deleted
    const userInDb = await User.findOne({ email: userData.email });
    expect(userInDb).toBeNull();
  });

  it("should return 404 if user does not exist", async () => {
    // Input: non-existent email; Expected: HTTP 404 and message "User not found".
    const res = await request(app).delete(`/notfound@example.com`);
    expect(res.status).toBe(404);
    expect(res.body.message).toBe("User not found");
  });
});
