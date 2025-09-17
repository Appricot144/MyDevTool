import { useState } from "react";

function FileImportBoard({
  setFile,
}: {
  setFile: (file: File | null) => void;
}) {
  const [isDragging, setIsDragging] = useState<boolean>(false);

  const onDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  };
  const onDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
  };
  const onDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    onChangeFile(e.dataTransfer.files);
  };

  const onChangeFile = (filelist: FileList | null) => {
    if (!filelist) {
      setFile(null);
      return;
    }

    const file = filelist[0];
    if (file?.size > 200 * 1024 * 1024) {
      alert("ファイルサイズが大きすぎます(200MB)");
      setFile(null);
    }

    setFile(file);
  };

  return (
    <div className="text-center">
      {/* Drop Zone */}
      <label htmlFor="fileImportBoard" className="cursor-pointer">
        <div
          className={`border-2 border-dashed rounded-lg p-8 ${
            isDragging ? "border-blue-500 bg-blue-50" : "border-gray-300"
          }`}
          onDragOver={onDragOver}
          onDragLeave={onDragLeave}
          onDrop={onDrop}
        >
          <input
            id="fileImportBoard"
            type="file"
            className="hidden"
            accept=".md"
            onChange={(e) => onChangeFile(e.target.files)}
            multiple={false}
            disabled={false}
          />

          <h3 className="text-lg font-medium text-gray-900 mb-2">
            ファイルをドロップしてください
          </h3>
          <p className="text-gray-600 mb-4">
            ファイルをドラッグ&ドロップするか、クリックして選択してください
          </p>
          <div className="text-sm text-gray-500">
            <p>最大ファイルサイズ: 200MB</p>
            <p>対応形式: .md</p>
          </div>
        </div>
      </label>
    </div>
  );
}

export default FileImportBoard;
