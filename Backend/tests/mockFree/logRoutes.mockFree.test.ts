import request from "supertest";
import express from "express";
import mongoose from "mongoose";
import router from "../../routes/logRoutes";
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


describe("POST /:petName logFeeding(no Mock)", () => {
    it("should log a feeding successfully", async () => {
        const user = {name: "Bob", email: "user@example.com", _id:"67c6a1b8c80f08d03d4757d9" };
        await new User(user).save();
        const pet = {name: "Fluffy", feedingTime: "08:00" };
        await new Pet(pet).save();
        const household = { _id: "67cfc90a16ca0bfb944f50de", 
            managerId: user._id, 
            name: "testHouse"
         };
         await new Household(household).save()

        const reqBody = { userEmail: user.email, householdId: household._id, feedingAmount: 25 };
        const res = await request(app).post(`/${pet.name}`).send(reqBody);

        expect(res.status).toBe(201);
        expect(res.body.message).toBe("Feeding logged successfully");

        const savedLog = await Log.findOne({ householdId: household._id });
        expect(savedLog).not.toBeNull();
    });

    it("should return 404 if user is not found", async () => {
        const user = {name: "Bob", email: "user@example.com", _id:"67c6a1b8c80f08d03d4757d9" };
        //await new User(user).save(); No user saved here
        const pet = {name: "Fluffy", feedingTime: "08:00" };
        await new Pet(pet).save();
        const household = { _id: "67cfc90a16ca0bfb944f50de", 
            managerId: user._id, 
            name: "testHouse"
         };
         await new Household(household).save()

        const reqBody = { userEmail: user.email, householdId: household._id };
        const res = await request(app).post(`/${pet.name}`).send(reqBody);
        expect(res.status).toBe(404);
        expect(res.body.message).toBe("User not found");
    });

    it("should return 404 if pet is not found", async () => {
        const user = {name: "Bob", email: "user@example.com", _id:"67c6a1b8c80f08d03d4757d9" };
        await new User(user).save();
        const pet = {name: "Fluffy", feedingTime: "08:00" };
        //await new Pet(pet).save(); dont save pet here
        const household = { _id: "67cfc90a16ca0bfb944f50de", 
            managerId: user._id, 
            name: "testHouse"
         };
         await new Household(household).save()

        const reqBody = { userEmail: user.email, householdId: household._id };
        const res = await request(app).post(`/${pet.name}`).send(reqBody);

        expect(res.status).toBe(404);
        expect(res.body.message).toBe("Pet not found");
    });

    it("should return 404 if household is not found", async () => {
        const user = {name: "Bob", email: "user@example.com", _id:"67c6a1b8c80f08d03d4757d9" };
        await new User(user).save();
        const pet = {name: "Fluffy", feedingTime: "08:00" };
        await new Pet(pet).save();
        const household = { _id: "67cfc90a16ca0bfb944f50de", 
            managerId: user._id, 
            name: "testHouse"
         };
        //await new Household(household).save() dont save household here

        const reqBody = { userEmail: user.email, householdId: household._id };
        const res = await request(app).post(`/${pet.name}`).send(reqBody);

        expect(res.status).toBe(404);
        expect(res.body.message).toBe("Household not found");
    });
});

describe("GET /pet/:petId - Get Pet Feeding History", () => {
    
    it("should retreive feedings successfully", async () => {
        const pet = {name: "Fluffy", feedingTime: "08:00" };
        await new Pet(pet).save();
        

        const log = { petName: pet.name, userName: "User1", timestamp: "2025-03-17T10:00:00Z", amount: 5, householdId: "67c6a1b8c80f08d03d4757d9"}
        await new Log(log).save();

        const res = await request(app).get(`/pet/${pet.name}`);

        expect(res.status).toBe(200);
        expect(res.body).toHaveLength(1);
        expect(res.body[0].userName).toBe("User1");
        
    });
});

describe("GET /user/:userEmail - Get User Feeding History", () => {
    
    it("should retreive feedings successfully", async () => {
        const user = {name: "Bob", email: "user@example.com", _id:"67c6a1b8c80f08d03d4757d9" };
        await new User(user).save();

        const pet = {name: "Fluffy", feedingTime: "08:00" };
        await new Pet(pet).save();
        

        const log = { petName: pet.name, userName: "Bob", timestamp: "2025-03-17T10:00:00Z", amount: 5, householdId: "67c6a1b8c80f08d03d4757d9"}
        await new Log(log).save();

        const res = await request(app).get(`/user/${user.email}`);

        expect(res.status).toBe(200);
        expect(res.body).toHaveLength(1);
        expect(res.body[0].petName).toBe(pet.name);
        
    });
    it("should return 404 when user not found", async () => {
        const user = {name: "Bob", email: "user@example.com", _id:"67c6a1b8c80f08d03d4757d9" };
        //await new User(user).save(); dont save user

        const pet = {name: "Fluffy", feedingTime: "08:00" };        

        const log = { petName: pet.name, userName: "Bob", timestamp: "2025-03-17T10:00:00Z", amount: 5, householdId: "67c6a1b8c80f08d03d4757d9"}
        await new Log(log).save();

        const res = await request(app).get(`/user/${user.email}`);

        expect(res.status).toBe(404);
        expect(res.body.message).toBe("User not found");
        
    });
});

describe("Get /household/:householdId logFeeding(no Mock)", () => {
    it("should retrieve logs successfully", async () => {
        const user = {name: "Bob", email: "user@example.com", _id:"67c6a1b8c80f08d03d4757d0" };
        const pet = {name: "Fluffy", feedingTime: "08:00" };
        const household = { _id: "67c6a1b8c80f08d03d4757d9", 
            managerId: user._id, 
            name: "testHouse"
            };
        await new Household(household).save()

        const log = { petName: pet.name, userName: "Bob", timestamp: "2025-03-17T10:00:00Z", amount: 5, householdId: household._id}
        await new Log(log).save();

        const res = await request(app).get(`/household/${household._id}`);

        expect(res.status).toBe(200);
        expect(res.body).toHaveLength(1);
        expect(res.body[0].petName).toBe(pet.name);
    });
});