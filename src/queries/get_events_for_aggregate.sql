
-- Get all events for a particular aggregate
SELECT Id, Aggregate_Id, Aggregate_Type, Action, Data
FROM Events
WHERE Aggregate_Id = :aggregateid
ORDER BY Id
