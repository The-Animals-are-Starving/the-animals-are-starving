import request from "supertest";
import express from "express";
import mongoose from "mongoose";
import router from "../../routes/petRoutes";
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

describe("Post /pet add pet", () => {
    it("should create a pet successfully", async () => {
        const household = { _id: "67cfc90a16ca0bfb944f50de", 
            managerId: "67c6a1b8c80f08d03d4757d9", 
            name: "testHouse"
            };
        await new Household(household).save()

        const pet = {name: "Fluffy", feedingTime: "08:00", householdId: household._id };
        const res = await request(app).post("/").send(pet);

        expect(res.status).toBe(201);
        expect(res.body.message).toBe("Pet added successfully");

        const dbPet = await Pet.findOne({ name: "Fluffy" });
        expect(dbPet).not.toBeNull();
    })
    it("should fail when no household", async () => {
        const household = { _id: "67cfc90a16ca0bfb944f50de", 
            managerId: "67c6a1b8c80f08d03d4757d9", 
            name: "testHouse"
            };
        //await new Household(household).save()

        const pet = {name: "Fluffy", feedingTime: "08:00", householdId: household._id };
        const res = await request(app).post("/").send(pet);

        expect(res.status).toBe(404);
        expect(res.body.message).toBe("Household not found");
    })
})

describe("Get /pet/:householdId getPets by household", () => {
    it("should retrieve pets by household successfully", async () => {
        const household = { _id: "67cfc90a16ca0bfb944f50de", 
            managerId: "67c6a1b8c80f08d03d4757d9", 
            name: "testHouse"
            };
        await new Household(household).save()

        const pet1 = {name: "Fluffy", feedingTime: "08:00", householdId: household._id };
        const pet2 = {name: "Fido", feedingTime: "08:00", householdId: household._id };

        const pets = {pet1, pet2};
        await new Pet(pet1).save();
        await new Pet(pet2).save();

        const res = await request(app).get(`/${household._id}`)

        expect(res.status).toBe(200);
        expect(res.body[0].name).toBe(pet1.name);
        expect(res.body[1].name).toBe(pet2.name);

    })
});

describe("PATCH /pet/:petName/feed update pet feeding", () => {
    it("should update the pet feeding", async () => {
        const household = { _id: "67cfc90a16ca0bfb944f50de", 
            managerId: "67c6a1b8c80f08d03d4757d9", 
            name: "testHouse"
            };
        await new Household(household).save()

        const pet1 = {name: "Fluffy", feedingTime: "08:00", householdId: household._id, fed: false };
        await new Pet(pet1).save();

        const reqBody = {fed: true};
        const res = await request(app).patch(`/${pet1.name}/feed`).send(reqBody)

        expect(res.status).toBe(200);
        expect(res.body.message).toBe("Pet feeding status updated");
        expect(res.body.pet.fed).toBe(true);

        // Verify the database was updated
        const updatedPet = await Pet.findOne({ name: "Fluffy" });
        expect(updatedPet!.fed).toBe(true);

    });

    it("should return 400 when no fed", async () => {
        const household = { _id: "67cfc90a16ca0bfb944f50de", 
            managerId: "67c6a1b8c80f08d03d4757d9", 
            name: "testHouse"
            };
        await new Household(household).save()

        const pet1 = {name: "Fluffy", feedingTime: "08:00", householdId: household._id, fed: false };
        await new Pet(pet1).save();

        const reqBody = {fed: true};
        const res = await request(app).patch(`/${pet1.name}/feed`)

        expect(res.status).toBe(400);
        expect(res.body.message).toBe("'fed' field is required");

    });

    it("should return 400 when fed is not a boolean", async () => {
        const household = { _id: "67cfc90a16ca0bfb944f50de", 
            managerId: "67c6a1b8c80f08d03d4757d9", 
            name: "testHouse"
            };
        await new Household(household).save()

        const pet1 = {name: "Fluffy", feedingTime: "08:00", householdId: household._id, fed: false };
        await new Pet(pet1).save();

        const reqBody = {fed: "hello"};
        const res = await request(app).patch(`/${pet1.name}/feed`).send(reqBody)

        expect(res.status).toBe(400);
        expect(res.body.message).toBe("'fed' must be a boolean");

        

    });

    it("should return 404 when pet is not found", async () => {
        const household = { _id: "67cfc90a16ca0bfb944f50de", 
            managerId: "67c6a1b8c80f08d03d4757d9", 
            name: "testHouse"
            };
        await new Household(household).save()

        const pet1 = {name: "Fluffy", feedingTime: "08:00", householdId: household._id, fed: false };
        //await new Pet(pet1).save();

        const reqBody = {fed: true};
        const res = await request(app).patch(`/${pet1.name}/feed`).send(reqBody)

        expect(res.status).toBe(404);
        expect(res.body.message).toBe("Pet not found");
    });
});

describe("DELETE /pet/:petName - removePet", () => {
    it("should delete the pet successfully", async () => {
        const household = { _id: "67cfc90a16ca0bfb944f50de", 
            managerId: "67c6a1b8c80f08d03d4757d9", 
            name: "testHouse"
            };
        await new Household(household).save()

        const pet1 = {name: "Fluffy", feedingTime: "08:00", householdId: household._id, fed: false };
        await new Pet(pet1).save();

        const reqBody = {fed: true};
        const res = await request(app).delete(`/${pet1.name}`)

        expect(res.status).toBe(200);
        expect(res.body).toBe(true);
        const pets = await Pet.find();
        expect(pets).toHaveLength(0);

        // Verify pet was removed from database
        const deletedPet = await Pet.findOne({ name: "Fluffy" });
        expect(deletedPet).toBeNull()
    });

    it("should return 404 when the pet is not found", async () => {
        const household = { _id: "67cfc90a16ca0bfb944f50de", 
            managerId: "67c6a1b8c80f08d03d4757d9", 
            name: "testHouse"
            };
        await new Household(household).save()

        const pet1 = {name: "Fluffy", feedingTime: "08:00", householdId: household._id, fed: false };
        //await new Pet(pet1).save();

        const reqBody = {fed: true};
        const res = await request(app).delete(`/${pet1.name}`)

        expect(res.status).toBe(404);
        expect(res.body.message).toBe("Pet not found");

    });
})

