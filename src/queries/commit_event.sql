
-- Add a new event to the event store
INSERT INTO Events(Aggregate_Type, Aggregate_Id, Data)
VALUES(:aggregatetype, :aggregateid, :data);
