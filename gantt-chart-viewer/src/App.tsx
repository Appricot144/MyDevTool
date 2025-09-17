import "./index.css";

import GanttChart from "./components/GanttChart";
import FileImportBoard from "./components/FileImportBoard";
import { useState } from "react";

function App() {
  const [file, setFile] = useState<File | null>(null);

  const onFileUpload = () => {};
  const onFileDelete = () => {};
  const onChangeFile = () => {};

  return (
    <div className="min-h-screen min-w-screen bg-gray-100 p-6">
      <div className="max-w-6xl mx-auto">
        <div className="space-y-9">
          <FileImportBoard setFile={(file: File | null) => setFile(file)} />
          <GanttChart file={file} />
        </div>
      </div>
    </div>
  );
}

export default App;
