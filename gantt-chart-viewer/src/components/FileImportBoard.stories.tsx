import type { Meta, StoryObj } from "@storybook/react-vite";

import FileImportBoard from "./FileImportBoard";
import "../index.css";

const meta = {
  component: FileImportBoard,
} satisfies Meta<typeof FileImportBoard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: { setFile: () => {} },
};
