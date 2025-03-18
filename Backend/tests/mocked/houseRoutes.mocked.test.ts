import request from "supertest";
import express from "express";
import householdRouter from "../../routes/userHouseholdRoutes";
import User from "../../models/User"; // Assume IUser and UserSchema are defined in this imported file
import Household from "../../models/Household";

// Mocks
jest.mock("../../models/User");
jest.mock("../../models/Household");

const app = express();
app.use(express.json());
app.use("/household", householdRouter);

describe("Household Management Tests", () => {
    let createdHouseholdId: string;

    // ========================== HOUSEHOLD CREATION TEST ==========================
    it("should create a new household successfully", async () => {
        const householdData = { householdName: "Test House", managerEmail: "manager@example.com" };

        (User.findOne as jest.Mock).mockResolvedValue({ _id: "manager123" });
        const saveMock = jest.fn().mockResolvedValue({
            _id: "house123",
            name: "Test House",
            managerId: "manager123",
            users: ["manager123"],
        });

        (Household as unknown as jest.Mock).mockImplementation((data) => ({
            ...data,
            save: saveMock,
        }));

        const res = await request(app).post("/household/create").send(householdData);

        expect(res.status).toBe(201);
        expect(res.body.name).toBe("Test House");
        createdHouseholdId = res.body._id;
        console.log("HouseID Returned: %s", createdHouseholdId)
        expect(saveMock).toHaveBeenCalled();
    });
    // ========================== USER NOT FOUND TEST ==========================
    it("should return 404 when manager user is not found", async () => {
        const householdData = { householdName: "Test House", managerEmail: "nonexistent@example.com" };

        (User.findOne as jest.Mock).mockResolvedValue(null);

        const res = await request(app).post("/household/create").send(householdData);

        expect(res.status).toBe(404);
        expect(res.body.message).toBe("Managing user not found");
    });

    // ========================== ERROR TESTS ==========================
    it("should return 500 if saving the household fails", async () => {
        const reqBody = { householdName: "Test House", managerEmail: "manager@example.com" };

        (User.findOne as jest.Mock).mockResolvedValue({ _id: "manager123" });

        const saveMock = jest.fn().mockRejectedValue(new Error("Database save error"));
        (Household as unknown as jest.Mock).mockImplementation(() => ({ save: saveMock }));

        const res = await request(app).post("/household/create").send(reqBody);

        expect(res.status).toBe(500);
        expect(res.body.message).toBe("An internal server error occurred");
        expect(saveMock).toHaveBeenCalled();
    });
});
