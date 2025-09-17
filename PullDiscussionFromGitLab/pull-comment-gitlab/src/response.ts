/**
 * gitlab api response
 */

interface Author {
  id: number;
  name: string;
  username: string;
  state: string;
  web_url: string;
}

interface ResMr {
  id: number;
  iid: number;
  project_id: string;
  title: string;
  create_at: Date;
  update_at: Date;
  author: Author;
}

interface ResDiscussion {
  id: number;
  indidual_note: boolean;
  notes: Note[];
}

interface Note {
  id: number;
  type: string;
  body: string;
  attachment: null;
  author: Author;
  created_at: Date;
  updated_at: Date;
  system: boolean;
  noteable_id: number;
  noteable_type: string;
  project_id: number;
  resolved: boolean;
  resolvable: boolean;
  resolved_by: null;
  resolved_at: null;
}

export { type ResMr, type Author, type ResDiscussion, type Note };
