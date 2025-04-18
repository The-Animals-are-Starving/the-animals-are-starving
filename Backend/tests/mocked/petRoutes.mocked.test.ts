import request from "supertest";
import Response from "supertest";
import express from "express";
import userRouter from "../../routes/userRoutes";
import householdRouter from "../../routes/userHouseholdRoutes";
import petRouter from "../../routes/petRoutes";
import User from "../../models/User";
import Household from "../../models/Household";
import Pet from "../../models/Pet";
import mongoose from 'mongoose';


// Mocks
jest.mock("../../models/User");
jest.mock("../../models/Household");
jest.mock("../../models/Pet");

const app = express();
app.use(express.json());
app.use("/user", userRouter);
app.use("/household", householdRouter);
app.use("/pet", petRouter);

describe("Full Flow: Create User ➔ Create Household ➔ Add/Manage Pets", () => {
  beforeEach(() => {
    (Household.findById as jest.Mock).mockResolvedValue({
        _id: "house123",
        name: "Test House",
        users: [createdUserId],
    });
  });
  afterEach(() => {
    jest.clearAllMocks();
  });


  // ========================== USER CREATION TEST ==========================
  let createdUserId: string;

  it("should create a new user successfully", async () => {
    const userData = { email: "manager@example.com", name: "Test Manager", householdId: undefined };

    (User.findOne as jest.Mock).mockResolvedValue(null);

    const saveMock = jest.fn().mockResolvedValue({ _id: "manager123", ...userData });
    (User as unknown as jest.Mock).mockImplementation((data) => ({
      ...data,
      save: saveMock,
    }));

    const res = await request(app).post("/user").send(userData);

    expect(res.status).toBe(201);
    expect(res.body.message).toBe("User created successfully");
    createdUserId = res.body.user._id;
    expect(saveMock).toHaveBeenCalled();
  });

  // ========================== HOUSEHOLD CREATION TEST ==========================
  let createdHouseholdId: string;

  it("should create a new household successfully when a valid manager exists", async () => {
    const householdData = { householdName: "Test House", managerEmail: "manager@example.com" };

    (User.findOne as jest.Mock).mockResolvedValue({ _id: createdUserId });

    const saveMock = jest.fn().mockResolvedValue({
      _id: "house123",
      name: "Test House",
      managerId: createdUserId,
      users: [createdUserId],
    });

    (Household as unknown as jest.Mock).mockImplementation((data) => ({
      ...data,
      save: saveMock,
    }));

    const res = await request(app).post("/household/create").send(householdData);
    
    expect(res.status).toBe(201);
    expect(res.body.name).toBe("Test House");
    createdHouseholdId = res.body._id;
    expect(saveMock).toHaveBeenCalled();
  });

  // ========================== PET CREATION TEST ==========================
  it("should add a new pet successfully when a valid householdId is provided", async () => {
    await new Promise((resolve) => setTimeout(resolve, 100));  // Ensure previous test completed
    console.log("CreateHouseholdId: %s", createdHouseholdId);
    const petData = { name: "Fluffy", householdId: createdHouseholdId, feedingTime: "08:00" };

    (Household.findById as jest.Mock).mockResolvedValue({ _id: createdHouseholdId });

    const saveMock = jest.fn().mockResolvedValue(petData);
    (Pet as unknown as jest.Mock).mockImplementation((data) => ({
      ...data,
      save: saveMock,
    }));

    const res = await request(app).post("/pet").send(petData);
    console.log("Creation: %s, mess: %s",res.body, res.body.message);  // Logs the full response body for debugging
    

    
    expect(res.status).toBe(201);
    expect(res.body.message).toBe("Pet added successfully");
    expect(res.body.pet.name).toBe("Fluffy");
    expect(Household.findById).toHaveBeenCalledWith(petData.householdId);
    expect(saveMock).toHaveBeenCalled();
  });

  // ========================== PET FAILED CREATION TEST: NO HOUSEHOLD ==========================
  it("should return 404 if no household is found when adding a pet", async () => {
    const petData = { name: "Fideo", householdId: "invalidHouseholdId", feedingTime: "08:00" };

    (Household.findById as jest.Mock).mockResolvedValue(null); // Simulate no household found

    const res = await request(app).post("/pet").send(petData);

    expect(res.status).toBe(404);
    expect(res.body.message).toBe("Household not found");
  });

  // ========================== PET FAILED CREATION TEST: FAILED SAVE ==========================
  it("should return 500 if the database fails to save", async () => {
    const petData = { name: "Fideo", householdId: "invalidHouseholdId", feedingTime: "08:00" };

    (Household.findById as jest.Mock).mockResolvedValue({ _id: createdHouseholdId });

    const saveMock = jest.fn().mockRejectedValue(new Error("Database save error"));
    (Pet as unknown as jest.Mock).mockImplementation(() => ({ save: saveMock }));

    const res = await request(app).post("/pet").send(petData);

    expect(res.status).toBe(500);
    expect(res.body.message).toBe("Error adding pet");
  });

  // ========================== GET PETS BY HOUSEHOLD TEST ==========================
  it("should return a list of pets for a household", async () => {

    const houseId = "67cfc99116ca0bfb944f50ec"
    console.log("GET PETS: %s", houseId);

    const pets = [{ name: "Fluffy", householdId: houseId }, { name: "Bella", householdId: houseId }];

    (Pet.find as jest.Mock).mockResolvedValue(pets);

    const res = await request(app).get(`/pet/${houseId}`);
    console.log("GET PETS: %s", res.body.message);
    expect(res.status).toBe(200);
    expect(res.body).toEqual(pets);
    expect(Pet.find).toHaveBeenCalledWith({ householdId: houseId });
  });

    // ========================== GET PETS BY HOUSEHOLD TEST: DATABASE FAIL ==========================
    it("should return a list of pets for a household", async () => {

      const houseId = "67cfc99116ca0bfb944f50ec"
      console.log("GET PETS: %s", houseId);
  
      const pets = [{ name: "Fluffy", householdId: houseId }, { name: "Bella", householdId: houseId }];
  
      (Pet.find as jest.Mock).mockRejectedValue(new Error("Database query error"));
  
      const res = await request(app).get(`/pet/${houseId}`);
      console.log("GET PETS: %s", res.body.message);
      expect(res.status).toBe(500);
      expect(res.body.message).toBe("Error retrieving pets");
    });

  // ========================== FEED PET TEST ==========================
  it("should update the pet's feeding status to true", async () => {
    const petName = "Fluffy";
    const updates = { fed: true };

    const petData = { name: petName, fed: false };

    (Pet.findOne as jest.Mock).mockResolvedValue(petData);
    const saveMock = jest.fn().mockResolvedValue({ ...petData, fed: true });
    (Pet.findByIdAndUpdate as jest.Mock).mockResolvedValue({ ...petData, fed: true });

    const res = await request(app).patch(`/pet/${petName}/feed`).send(updates);
    expect(res.status).toBe(200);
    expect(res.body.message).toBe("Pet feeding status updated");
    expect(res.body.pet.fed).toBe(true);
    expect(Pet.findOne).toHaveBeenCalledWith({ name: petName });
    expect(Pet.findByIdAndUpdate).toHaveBeenCalled();
  });

  // ========================== FEED PET TEST FAILURE: NO FED ==========================
  it("should return 400 if no fed field", async () => {
    const petData = { name: "Fideo", householdId: "invalidHouseholdId", feedingTime: "08:00" };

    (Household.findById as jest.Mock).mockResolvedValue(null); // Simulate no household found

    const res = await request(app).patch(`/pet/${petData.name}/feed`);


    expect(res.status).toBe(400);
    expect(res.body.message).toBe("'fed' field is required");
  });

  // ========================== FEED PET TEST FAILURE: BAD FED ==========================
  it("should return 400 if fed is not a boolean", async () => {
    const petData = { name: "Fideo", householdId: "invalidHouseholdId", feedingTime: "08:00" };
    const updates = { fed: "hello" };


    const res = await request(app).patch(`/pet/${petData.name}/feed`).send(updates);

    expect(res.status).toBe(400);
    expect(res.body.message).toBe("'fed' must be a boolean");
  });

  // ========================== FEED PET TEST FAILURE: PET NOT FOUND ==========================
  it("should return 404 if no pet is found", async () => {
    const petName = "Fluffy";
    const updates = { fed: true };

    const petData = { name: petName, fed: false };

    (Pet.findOne as jest.Mock).mockResolvedValue(null);

    const res = await request(app).patch(`/pet/${petName}/feed`).send(updates);
    expect(res.status).toBe(404);
    expect(res.body.message).toBe("Pet not found");
    expect(Pet.findOne).toHaveBeenCalledWith({name: petName});
  });


  // ========================== FEED PET TEST FAILURE: PET NOT FOUND FROM ID==========================
  it("should return 404 if no pet is found during update", async () => {
    const petName = "Fluffy";
    const updates = { fed: true };

    const petData = {_id: "pet123", name: petName, fed: false };

    (Pet.findOne as jest.Mock).mockResolvedValue(petData);
    (Pet.findByIdAndUpdate as jest.Mock).mockResolvedValue(null);

    const res = await request(app).patch(`/pet/${petName}/feed`).send(updates);
    expect(res.status).toBe(404);
    expect(res.body.message).toBe("Pet not found from id");
    expect(Pet.findOne).toHaveBeenCalledWith({ name: petName });
    expect(Pet.findByIdAndUpdate).toHaveBeenCalled();
  });

  // ========================== FEED PET TEST FAIL: DATABASE UPDATE FAILURE ==========================
  it("should update the pet's feeding status to true", async () => {
    const petName = "Fluffy";
    const updates = { fed: true };

    const petData = { name: petName, fed: false };

    (Pet.findOne as jest.Mock).mockResolvedValue(petData);
    (Pet.findByIdAndUpdate as jest.Mock).mockRejectedValue(new Error("Database query error"));

    const res = await request(app).patch(`/pet/${petName}/feed`).send(updates);
    expect(res.status).toBe(500);
    expect(res.body.message).toBe("Error updating pet feeding status");
    expect(Pet.findOne).toHaveBeenCalledWith({ name: petName });
    expect(Pet.findByIdAndUpdate).toHaveBeenCalled();
  });


  // ========================== REMOVE PET TEST ==========================
  it("should remove the pet successfully", async () => {
    const petName = "Fluffy";
    const petData = { name: petName, _id:"pet123" };

    (Pet.findOne as jest.Mock).mockResolvedValue(petData);
    const removeMock = jest.fn().mockResolvedValue(petData);
    (Pet.findByIdAndDelete as jest.Mock).mockResolvedValue(petData);

    const res = await request(app).delete(`/pet/${petName}`);
    expect(res.status).toBe(200);
    expect(res.body).toBe(true);
    expect(Pet.findOne).toHaveBeenCalledWith({ name: petName });
    expect(Pet.findByIdAndDelete).toHaveBeenCalledWith("pet123");
  });

  // ========================== REMOVE PET TEST FAIL: No pet found 1==========================
  it("should remove the pet successfully", async () => {
    const petName = "Fluffy";
    const petData = { name: petName };

    (Pet.findOne as jest.Mock).mockResolvedValue(null);
    const removeMock = jest.fn().mockResolvedValue(petData);
    (Pet.findByIdAndDelete as jest.Mock).mockResolvedValue(petData);

    const res = await request(app).delete(`/pet/${petName}`);
    expect(res.status).toBe(404);
    expect(res.body.message).toBe("Pet not found");
    expect(Pet.findOne).toHaveBeenCalledWith({ name: petName });
  });

    // ========================== REMOVE PET TEST FAIL: No pet found 2==========================
    it("should remove the pet successfully", async () => {
      const petName = "Fluffy";
      const petData = { name: petName, _id:"pet123"};
  
      (Pet.findOne as jest.Mock).mockResolvedValue(petData);
      const removeMock = jest.fn().mockResolvedValue(petData);
      (Pet.findByIdAndDelete as jest.Mock).mockResolvedValue(null);
  
      const res = await request(app).delete(`/pet/${petName}`);
      expect(res.status).toBe(404);
      expect(res.body.message).toBe("Pet not found");
      expect(Pet.findOne).toHaveBeenCalledWith({ name: petName });
      expect(Pet.findByIdAndDelete).toHaveBeenCalledWith("pet123");
    });

    // ========================== REMOVE PET TEST FAIL: DATABASE ERROR==========================
  it("should remove the pet successfully", async () => {
    const petName = "Fluffy";
    const petData = { name: petName, _id:"pet123" };

    (Pet.findOne as jest.Mock).mockResolvedValue(petData);
    const removeMock = jest.fn().mockResolvedValue(petData);
    (Pet.findByIdAndDelete as jest.Mock).mockRejectedValue(new Error("Database query error"));

    const res = await request(app).delete(`/pet/${petName}`);
    expect(res.status).toBe(500);
    expect(res.body.message).toBe("Error removing pet");
    expect(Pet.findOne).toHaveBeenCalledWith({ name: petName });
    expect(Pet.findByIdAndDelete).toHaveBeenCalledWith("pet123");
  });
});


