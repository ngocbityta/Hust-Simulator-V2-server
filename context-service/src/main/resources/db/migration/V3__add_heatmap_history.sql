CREATE TABLE heatmap_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cell_x INT NOT NULL,
    cell_y INT NOT NULL,
    average_count INT NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_heatmap_history_cell ON heatmap_history(cell_x, cell_y);
CREATE INDEX idx_heatmap_history_time ON heatmap_history(recorded_at);
