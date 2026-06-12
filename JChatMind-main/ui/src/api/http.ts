import { message } from "antd";

// API ??????,???? ApiResponse ??
export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
}

// ??????
export interface RequestOptions extends RequestInit {
  params?: Record<string, string | number | boolean | null | undefined>;
}

// API ????(???????,?? http://localhost:8080/api)
export const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

/**
 * ????? URL(??????)
 */
function buildUrl(url: string, params?: Record<string, string | number | boolean | null | undefined>): string {
  const fullUrl = `${BASE_URL}${url}`;
  
  if (!params || Object.keys(params).length === 0) {
    return fullUrl;
  }

  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined) {
      searchParams.append(key, String(value));
    }
  });

  const queryString = searchParams.toString();
  return queryString ? `${fullUrl}?${queryString}` : fullUrl;
}

/**
 * ????
 */
async function handleResponse<T>(response: Response): Promise<ApiResponse<T>> {
  if (!response.ok) {
    // HTTP ?????
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  const data: ApiResponse<T> = await response.json();

  // ???????
  if (data.code !== 200) {
    message.error(data.message || "????");
    throw new Error(data.message || "????");
  }

  return data;
}

/**
 * ??? fetch ????
 */
async function request<T = unknown>(
  url: string,
  options: RequestOptions = {}
): Promise<T> {
  const { params, headers, ...restOptions } = options;

  // ???? URL
  const fullUrl = buildUrl(url, params);

  // ???????
  const defaultHeaders: HeadersInit = {
    "Content-Type": "application/json",
    ...headers,
  };

  try {
    const response = await fetch(fullUrl, {
      ...restOptions,
      headers: defaultHeaders,
    });

    const apiResponse = await handleResponse<T>(response);
    return apiResponse.data;
  } catch (error) {
    // ??????
    if (error instanceof Error) {
      throw error;
    }
    throw new Error("??????");
  }
}

/**
 * GET ??
 */
export function get<T = unknown>(
  url: string,
  params?: Record<string, string | number | boolean | null | undefined>,
  options?: Omit<RequestOptions, "method" | "body" | "params">
): Promise<T> {
  return request<T>(url, {
    ...options,
    method: "GET",
    params,
  });
}

/**
 * POST ??
 */
export function post<T = unknown>(
  url: string,
  data?: unknown,
  options?: Omit<RequestOptions, "method" | "body">
): Promise<T> {
  return request<T>(url, {
    ...options,
    method: "POST",
    body: data ? JSON.stringify(data) : undefined,
  });
}

/**
 * PUT ??
 */
export function put<T = unknown>(
  url: string,
  data?: unknown,
  options?: Omit<RequestOptions, "method" | "body">
): Promise<T> {
  return request<T>(url, {
    ...options,
    method: "PUT",
    body: data ? JSON.stringify(data) : undefined,
  });
}

/**
 * PATCH ??
 */
export function patch<T = unknown>(
  url: string,
  data?: unknown,
  options?: Omit<RequestOptions, "method" | "body">
): Promise<T> {
  return request<T>(url, {
    ...options,
    method: "PATCH",
    body: data ? JSON.stringify(data) : undefined,
  });
}

/**
 * DELETE ??
 */
export function del<T = unknown>(
  url: string,
  params?: Record<string, string | number | boolean | null | undefined>,
  options?: Omit<RequestOptions, "method" | "body" | "params">
): Promise<T> {
  return request<T>(url, {
    ...options,
    method: "DELETE",
    params,
  });
}

// ??????,????
export default {
  get,
  post,
  put,
  patch,
  delete: del,
};
