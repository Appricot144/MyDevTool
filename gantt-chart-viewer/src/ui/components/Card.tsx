import type { ReactNode } from "react";

type CardProps = {
  titleArea?: ReactNode;
  subtitle?: string;
  children?: ReactNode;
  footArea?: ReactNode;
};

function Card({ titleArea, subtitle, children, footArea }: CardProps) {
  return (
    <div className="p-6 bg-white border border-gray-200 rounded-lg shadow-sm md-2">
      {titleArea && <div className="title-area">{titleArea}</div>}
      <div className="subtitle mb-2 font-normal text-gray-500">{subtitle}</div>
      <div className="content mb-2 font-normal text-gray-700">{children}</div>
      {footArea && (
        <div className="footer pt-2 border-t border-gray-100 text-gray-700">
          {footArea}
        </div>
      )}
    </div>
  );
}

export default Card;
