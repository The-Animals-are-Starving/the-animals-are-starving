import express from "express";
import { createUser, getAllUsers, getUser, updateUserRole, deleteUser, updateUserHouseholdId } from "../controllers/userController";

const router = express.Router();

router.post("/", createUser);
router.get("/:householdId", getAllUsers);
router.get("/specific-user/:email", getUser);
router.patch("/update-household/:email", updateUserHouseholdId);
router.patch("/:email", updateUserRole);
router.delete("/:email", deleteUser);

export default router;
