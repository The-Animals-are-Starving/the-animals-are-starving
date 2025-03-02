import express from "express";
import { createUser, getAllUsers, getUser, updateUserRole, deleteUser } from "../controllers/userController";

const router = express.Router();

router.post("/", createUser);
router.get("/:householdId", getAllUsers);
router.get("/:email", getUser);
router.patch("/:email", updateUserRole);
router.delete("/:email", deleteUser);

export default router;
