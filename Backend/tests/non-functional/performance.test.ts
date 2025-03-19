import request from "supertest";
import express from "express";
import mongoose, { Types } from "mongoose";
import { MongoMemoryServer } from "mongodb-memory-server";
import userRoutes from "../../routes/userRoutes";
import petRoutes from "../../routes/petRoutes";
import householdRoutes from "../../routes/userHouseholdRoutes";
import User from "../../models/User";

const app = express();
app.use(express.json());

app.use("/users", userRoutes);
app.use("/pets", petRoutes);
app.use("/households", householdRoutes);

let mongoServer: MongoMemoryServer;

beforeAll(async () => {
  mongoServer = await MongoMemoryServer.create();
  const uri = mongoServer.getUri();
  await mongoose.connect(uri);

  const dummyHouseholdId = new Types.ObjectId();
  await mongoose.connection.collection("users").insertOne({
    email: "dummy@example.com",
    name: "Dummy User",
    householdId: dummyHouseholdId,
    role: "normal",
  });
  await mongoose.connection.collection("pets").insertOne({
    petName: "Fluffy",
    householdId: dummyHouseholdId,
    feedingStatus: "hungry",
  });
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});


jest.setTimeout(30000);

describe("Performance Non-Functional Tests", () => {
  const numConcurrentRequests = 100;
  const targetTimeMs = 2000;

  it("should respond quickly for concurrent calls to user endpoints", async () => {
    const householdId = new Types.ObjectId().toHexString();
    const requests = [];
    const startTime = Date.now();
    for (let i = 0; i < numConcurrentRequests; i++) {
      requests.push(request(app).get(`/users/${householdId}`));
    }
    const responses = await Promise.all(requests);
    const totalTime = Date.now() - startTime;
    console.log(`User endpoints: Total time for ${numConcurrentRequests} concurrent requests: ${totalTime} ms`);
    expect(totalTime).toBeLessThan(targetTimeMs);
    responses.forEach(res => {
      expect(res.status).toBe(200);
    });
  });

  it("should respond quickly for concurrent calls to pet endpoints", async () => {
    const householdId = new Types.ObjectId().toHexString();
    const requests = [];
    const startTime = Date.now();
    for (let i = 0; i < numConcurrentRequests; i++) {
      requests.push(request(app).get(`/pets/${householdId}`));
    }
    const responses = await Promise.all(requests);
    const totalTime = Date.now() - startTime;
    console.log(`Pet endpoints: Total time for ${numConcurrentRequests} concurrent requests: ${totalTime} ms`);
    expect(totalTime).toBeLessThan(targetTimeMs);
    responses.forEach(res => {
      expect(res.status).toBe(200);
    });
  });

  it("should respond quickly for concurrent calls to household endpoints", async () => {
    const managerEmail = "manager@example.com";
    await User.create({ email: managerEmail, name: "Manager", role: "manager" });

    const payload = { householdName: "Test Household", managerEmail };
    const requests = [];
    const startTime = Date.now();
    for (let i = 0; i < numConcurrentRequests; i++) {
      requests.push(request(app).post(`/households/create`).send(payload));
    }
    const responses = await Promise.all(requests);
    const totalTime = Date.now() - startTime;
    console.log(`Household endpoints: Total time for ${numConcurrentRequests} concurrent requests: ${totalTime} ms`);
    expect(totalTime).toBeLessThan(targetTimeMs);
    responses.forEach(res => {
      expect(res.status).toBe(201);
    });
  });
});