export interface CustomHttpResponse {
  timestamp: Date;
  httpStatusCode: number;
  httpStatus: string;
  reason: string;
  message: string;
}
