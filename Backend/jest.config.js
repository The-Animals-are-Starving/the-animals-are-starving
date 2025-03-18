module.exports = {
    preset: 'ts-jest',
    testEnvironment: 'node',
    collectCoverageFrom: [
      "controllers/**/*.ts",
      "routes/**/*.ts",
      "models/**/*.ts",
      "logs/**/*.ts"
    ],
  };
  