CREATE TABLE IF NOT EXISTS context.facility_issues (
    id UUID PRIMARY KEY,
    building_id UUID NOT NULL,
    room_id UUID,
    reporter_id UUID NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    resolved_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

CREATE INDEX idx_facility_issues_building_id ON context.facility_issues(building_id);
CREATE INDEX idx_facility_issues_status ON context.facility_issues(status);
