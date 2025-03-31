import request from "supertest";
import express from "express";
import logRouter from "../../routes/logRoutes";
import Log from "../../models/Log";
import User from "../../models/User";
import Pet from "../../models/Pet";
import Household from "../../models/Household";

jest.mock("../../models/Log");
jest.mock("../../models/User");
jest.mock("../../models/Pet");
jest.mock("../../models/Household");

const app = express();
app.use(express.json());
app.use("/log", logRouter);

describe("Log Tests", () => {

    beforeEach(() => {
        jest.clearAllMocks();
    });

    it("should log a feeding successfully", async () => {
        const reqBody = { userEmail: "user@example.com", householdId: "67cfc90a16ca0bfb944f50de" };
        const petName = "Fluffy";

        (User.findOne as jest.Mock).mockResolvedValue({ _id: "user123", name: "Test User" });
        (Pet.findOne as jest.Mock).mockResolvedValue({ _id: "pet123", name: petName });
        (Household.findById as jest.Mock).mockResolvedValue({ _id: "67cfc90a16ca0bfb944f50de" });

        const saveMock = jest.fn().mockResolvedValue({ _id: "log123", ...reqBody, petName });
        (Log as unknown as jest.Mock).mockImplementation(() => ({ save: saveMock }));

        const res = await request(app).post(`/log/${petName}`).send(reqBody);

        expect(res.status).toBe(201);
        expect(res.body.message).toBe("Feeding logged successfully");
        expect(saveMock).toHaveBeenCalled();
    });

    it("should return 404 if user is not found", async () => {
        const reqBody = { userEmail: "missinguser@example.com", householdId: "house123" };
        const petName = "Fluffy";

        (User.findOne as jest.Mock).mockResolvedValue(null);

        const res = await request(app).post(`/log/${petName}`).send(reqBody);

        expect(res.status).toBe(404);
        expect(res.body.message).toBe("User not found");
    });

    it("should return 404 if pet is not found", async () => {
        const reqBody = { userEmail: "user@example.com", householdId: "house123" };
        const petName = "MissingPet";

        (User.findOne as jest.Mock).mockResolvedValue({ _id: "user123" });
        (Pet.findOne as jest.Mock).mockResolvedValue(null);

        const res = await request(app).post(`/log/${petName}`).send(reqBody);

        expect(res.status).toBe(404);
        expect(res.body.message).toBe("Pet not found");
    });

    it("should return 404 is household not found", async () => {
        const reqBody = { userEmail: "user@example.com", householdId: "67cfc90a16ca0bfb944f50de" };
        const petName = "Fluffy";

        (User.findOne as jest.Mock).mockResolvedValue({ _id: "user123", name: "Test User" });
        (Pet.findOne as jest.Mock).mockResolvedValue({ _id: "pet123", name: petName });
        (Household.findById as jest.Mock).mockResolvedValue(null);

        const saveMock = jest.fn().mockResolvedValue({ _id: "log123", ...reqBody, petName });
        (Log as unknown as jest.Mock).mockImplementation(() => ({ save: saveMock }));

        const res = await request(app).post(`/log/${petName}`).send(reqBody);

        expect(res.status).toBe(404);
        expect(res.body.message).toBe("Household not found");
    });

    it("should return 500 if log fails to save", async () => {
        const reqBody = { userEmail: "user@example.com", householdId: "67cfc90a16ca0bfb944f50de" };
        const petName = "Fluffy";

        (User.findOne as jest.Mock).mockResolvedValue({ _id: "user123", name: "Test User" });
        (Pet.findOne as jest.Mock).mockResolvedValue({ _id: "pet123", name: petName });
        (Household.findById as jest.Mock).mockResolvedValue({ _id: "67cfc90a16ca0bfb944f50de" });

        const saveMock = jest.fn().mockRejectedValue(new Error("Database save error"));
        (Log as unknown as jest.Mock).mockImplementation(() => ({ save: saveMock }));

        const res = await request(app).post(`/log/${petName}`).send(reqBody);

        expect(res.status).toBe(500);
        expect(res.body.message).toBe("Error logging feeding");
    });

    it("should return the pet's feeding history", async () => {
        const petName = "fido";
        const logs = [
            { petName, userName: "User1", timestamp: "2025-03-17T10:00:00Z", amount: 5 },
            { petName, userName: "User2", timestamp: "2025-03-16T10:00:00Z", amount: 3 }
        ];

        (Log.find as jest.Mock).mockReturnValue({
            sort: jest.fn().mockResolvedValue(logs)
        });

        const res = await request(app).get(`/log/pet/${petName}`);

        expect(res.status).toBe(200);
        expect(res.body).toEqual(logs);
        expect(Log.find).toHaveBeenCalledWith({ petName });
    });

    it("should fail to return the pet's feeding history when db fails", async () => {
        const petName = "pet123";
        const logs = [
            { petName, userName: "User1", timestamp: "2025-03-17T10:00:00Z", amount: 5 },
            { petName, userName: "User2", timestamp: "2025-03-16T10:00:00Z", amount: 3 }
        ];

        const saveMock = jest.fn().mockRejectedValue(new Error("Database save error"));
        (Log.find as jest.Mock).mockReturnValue({
            sort: jest.fn().mockImplementation(saveMock)
        });

        const res = await request(app).get(`/log/pet/${petName}`);

        expect(res.status).toBe(500);
        expect(res.body.message).toBe("Error retrieving feeding history");
        expect(Log.find).toHaveBeenCalledWith({ petName: petName });
    });

    it("should return the household feeding history", async () => {
        const householdId = "house123";
        const logs = [
            { householdId, petName: "Fluffy", timestamp: "2025-03-17T10:00:00Z", amount: 5 },
            { householdId, petName: "Bella", timestamp: "2025-03-16T10:00:00Z", amount: 3 }
        ];

        (Log.find as jest.Mock).mockReturnValue({
            sort: jest.fn().mockResolvedValue(logs)
            });

        const res = await request(app).get(`/log/household/${householdId}`);

        expect(res.status).toBe(200);
        expect(res.body).toEqual(logs);
        expect(Log.find).toHaveBeenCalledWith({ householdId });
    });
    it("should fail to with 500 return the household feeding history when db fails", async () => {
        const householdId = "house123";
        const logs = [
            { householdId, petName: "Fluffy", timestamp: "2025-03-17T10:00:00Z", amount: 5 },
            { householdId, petName: "Bella", timestamp: "2025-03-16T10:00:00Z", amount: 3 }
        ];
        const saveMock = jest.fn().mockRejectedValue(new Error("Database save error"));
        (Log.find as jest.Mock).mockReturnValue({
            sort: jest.fn().mockImplementation(saveMock)
            });

        const res = await request(app).get(`/log/household/${householdId}`);

        expect(res.status).toBe(500);
        expect(res.body.message).toEqual("Error retrieving household feeding history");
        expect(Log.find).toHaveBeenCalledWith({ householdId });
    });

    it("should return the user's feeding history", async () => {
        const userEmail = "user@example.com";
        const logs = [
            { userId: "user123", petName: "Fluffy", timestamp: "2025-03-17T10:00:00Z", amount: 5 },
            { userId: "user123", petName: "Bella", timestamp: "2025-03-16T10:00:00Z", amount: 3 }
        ];

        (User.findOne as jest.Mock).mockResolvedValue({ _id: "user123" });
        (Log.find as jest.Mock).mockReturnValue({
            sort: jest.fn().mockResolvedValue(logs)
        });

        const res = await request(app).get(`/log/user/${userEmail}`);

        expect(res.status).toBe(200);
        expect(res.body).toEqual(logs);
        expect(Log.find).toHaveBeenCalledWith({ userName: undefined });
    });

    it("should fail to return the user's feeding history w 404 when user not found ", async () => {
        const userEmail = "user@example.com";
        const logs = [
            { userId: "user123", petName: "Fluffy", timestamp: "2025-03-17T10:00:00Z", amount: 5 },
            { userId: "user123", petName: "Bella", timestamp: "2025-03-16T10:00:00Z", amount: 3 }
        ];

        (User.findOne as jest.Mock).mockResolvedValue(null);
        (Log.find as jest.Mock).mockReturnValue({
            sort: jest.fn().mockResolvedValue(logs)
        });

        const res = await request(app).get(`/log/user/${userEmail}`);

        expect(res.status).toBe(404);
        expect(res.body.message).toEqual("User not found");
        expect(User.findOne).toHaveBeenCalledWith({email: "user@example.com"});
    });

    it("should fail with 500 for user's feeding history when db fails", async () => {
        const userEmail = "user@example.com";
        const logs = [
            { userId: "user123", petName: "Fluffy", timestamp: "2025-03-17T10:00:00Z", amount: 5 },
            { userId: "user123", petName: "Bella", timestamp: "2025-03-16T10:00:00Z", amount: 3 }
        ];

        (User.findOne as jest.Mock).mockResolvedValue({ _id: "user123" });
        const saveMock = jest.fn().mockRejectedValue(new Error("Database save error"));
        (Log.find as jest.Mock).mockReturnValue({
            populate: jest.fn().mockReturnValue({
                sort: jest.fn().mockImplementation(saveMock)
            })
        });

        const res = await request(app).get(`/log/user/${userEmail}`);

        expect(res.status).toBe(500);
        expect(res.body.message).toEqual("Error retrieving user feeding history");
        expect(Log.find).toHaveBeenCalledWith({ userName: undefined });
    });
});
