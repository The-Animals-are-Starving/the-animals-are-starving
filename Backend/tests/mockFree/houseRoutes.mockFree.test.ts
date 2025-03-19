import request from "supertest";
import express from "express";
import mongoose from "mongoose";
import router from "../../routes/userHouseholdRoutes";
import Log from "../../models/Log";
import User from "../../models/User";
import Pet from "../../models/Pet";
import Household from "../../models/Household";
import { MongoMemoryServer } from "mongodb-memory-server";
import { UserMetadata } from "firebase-admin/lib/auth/user-record";

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
    await Log.deleteMany({});
    await User.deleteMany({});
    await Pet.deleteMany({});
    await Household.deleteMany({});
});

describe("POST /household/create - createHousehold", () => {
    it("Should create the house successfully", async () => {
        const user = {name: "Bob", email: "user@example.com", _id:"67c6a1b8c80f08d03d4757d9" };
        await new User(user).save();
        
        const reqBody = {householdName: "TestHouse", managerEmail: "user@example.com"};
        const res = (await request(app).post('/create').send(reqBody));

        expect(res.status).toBe(201);
        expect(res.body.name).toBe("TestHouse");
        expect(res.body.managerId).toBe(user._id);
    });

    it("Should return 404 if manager not found", async () => {
        const user = {name: "Bob", email: "user@example.com", _id:"67c6a1b8c80f08d03d4757d9" };
        //await new User(user).save();
        
        const reqBody = {householdName: "TestHouse", managerEmail: "user@example.com"};
        const res = (await request(app).post('/create').send(reqBody));

        expect(res.status).toBe(404);
        expect(res.body.message).toBe("Managing user not found");
    })
})