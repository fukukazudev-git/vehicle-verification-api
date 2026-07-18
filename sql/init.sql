CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    displayName VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    modelCode VARCHAR(50) NOT NULL UNIQUE,
    modelName VARCHAR(100) NOT NULL,
    modelYear INT,
    ecuType VARCHAR(50),
    engineType VARCHAR(50),
    driveType VARCHAR(20),
    description TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS review_meetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    modelId BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    scheduledDate DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '予定',
    organizerId BIGINT NOT NULL,
    notes TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (modelId) REFERENCES models(id),
    FOREIGN KEY (organizerId) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS test_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reviewMeetingId BIGINT NOT NULL,
    testName VARCHAR(200) NOT NULL,
    result VARCHAR(20) NOT NULL DEFAULT '保留',
    notes TEXT,
    recordedBy BIGINT NOT NULL,
    recordedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reviewMeetingId) REFERENCES review_meetings(id),
    FOREIGN KEY (recordedBy) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reviewMeetingId BIGINT,
    testRecordId BIGINT,
    file_name VARCHAR(255) NOT NULL,
    storedPath VARCHAR(500) NOT NULL,
    fileType VARCHAR(50),
    uploadedBy BIGINT NOT NULL,
    uploadedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reviewMeetingId) REFERENCES review_meetings(id),
    FOREIGN KEY (testRecordId) REFERENCES test_records(id),
    FOREIGN KEY (uploadedBy) REFERENCES users(id)
);