import Card from "../ui/components/Card";
import type { Task } from "../feature/Task";
import { useState } from "react";

type GanttChartData = {
  title: string;
  tasks: Task[];
  timeRanges: TimeRanges;
  category: object;
};

function GanttChart({ file }: { file: File | null }) {
  const [data, setData] = useState<GanttChartData>();
  return (
    <Card
      titleArea={
        <h5 className="text-2xl font-bold tracking-width text-gray-900">
          Task Gantt Chart
        </h5>
      }
      subtitle={""}
      footArea={""}
    >
      <div className="space-y-2">
        <div>
          <div></div>
          <div></div>
        </div>
        {data &&
          data.tasks.map((task) => (
            <div className="border-1 border-bottom">
              <div>{task.title}</div>
              <div></div>
            </div>
          ))}
      </div>
    </Card>
  );
}

export default GanttChart;
