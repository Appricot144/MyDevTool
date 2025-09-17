async function postFetcher<T>(url: string, body: string): Promise<T> {
  try {
    const res = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

    isErrorResponse(res);

    return (await res.json()) as T;
  } catch (error) {
    console.error(`POST request network error: ${error}`);
    throw error;
  }
}

async function getFetcher<T>(url: string): Promise<T> {
  try {
    const res = await fetch(url);

    isErrorResponse(res);

    return (await res.json()) as T;
  } catch (error) {
    console.error(`GET request network error: ${error}`);
    throw error;
  }
}

function isErrorResponse(res: Response): boolean {
  if (res.ok) {
    return false;
  }

  if (!res.ok) {
    throw new Error(`response status: ${res.statusText}`);
  }

  return true;
}

export { getFetcher, postFetcher };
