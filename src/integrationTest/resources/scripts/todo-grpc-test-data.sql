DELETE FROM todo_item;

INSERT INTO todo_item (id, description, priority, due_date)
VALUES (1, 'Task AAA', 'LOW', '2024-12-01'),
       (2, 'Task BBB', 'LOW', '2024-12-02'),
       (3, 'Task CCC', 'MEDIUM', null),
       (5, 'Task DDD', 'HIGH', '2024-08-10'),
       (6, 'Task EEE', 'MEDIUM', null),
       (7, 'Task FFF', null, null);

INSERT INTO todo_item(id, description, priority, due_date, completed, completed_at)
VALUES (20, 'A Completed Task', 'MEDIUM', '2024-05-15', true, CURRENT_TIMESTAMP);
