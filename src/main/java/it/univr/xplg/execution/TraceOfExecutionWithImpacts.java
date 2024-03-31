package it.univr.xplg.execution;

import it.univr.xplg.model.TaskWithImpacts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TraceOfExecutionWithImpacts
{
    private long _thisTime;
    private List<TaskWithImpacts> _executedTasks;

    public TraceOfExecutionWithImpacts(long thisTime)
    {
        _thisTime = thisTime;
        _executedTasks = new ArrayList<TaskWithImpacts>();
    }

    public TraceOfExecutionWithImpacts clone()
    {
        TraceOfExecutionWithImpacts result = new TraceOfExecutionWithImpacts(_thisTime);
        result.addAllExecutedTasks(_executedTasks);
        return result;
    }

    public void addTime(long otherTime)
    {
        _thisTime = _thisTime + otherTime;
    }
    public long get_thisTime() {
        return _thisTime;
    }

    public int addExecutedTask(TaskWithImpacts t)
    {
        _executedTasks.add(t);
        int size = _executedTasks.size();
        return size;
    }

    public int addAllExecutedTasks(Collection<TaskWithImpacts> ts)
    {
        _executedTasks.addAll(ts);
        int size = _executedTasks.size();
        return size;
    }

    public int removeExecutedTask(TaskWithImpacts t)
    {
        if(_executedTasks.contains(t))
        {
            _executedTasks.remove(t);
        }
        int size = _executedTasks.size();
        return size;
    }

    public List<TaskWithImpacts> get_executedTasks() {
        return _executedTasks;
    }
}
