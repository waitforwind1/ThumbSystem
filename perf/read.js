import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8082';
const mode = __ENV.MODE || 'list';
const blogId = __ENV.BLOG_ID;
const businessFailures = new Rate('business_failures');
const failureReasons = new Counter('failure_reasons');
const successDuration = new Trend('success_duration', true);
const failureDuration = new Trend('failure_duration', true);
const expectNonempty = __ENV.EXPECT_NONEMPTY !== '0';
let printedFailure = false;

if (mode !== 'list' && mode !== 'detail') {
  throw new Error('MODE must be list or detail');
}
if (mode === 'detail' && !blogId) {
  throw new Error('BLOG_ID is required when MODE=detail');
}

export const options = {
  scenarios: {
    read: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.RATE || 10),
      timeUnit: '1s',
      duration: __ENV.DURATION || '1m',
      preAllocatedVUs: Number(__ENV.VUS || 20),
      maxVUs: Number(__ENV.MAX_VUS || 100),
    },
  },
  thresholds: {
    business_failures: ['rate<0.01'],
    dropped_iterations: ['count==0'],
  },
};

export default function () {
  const url = mode === 'detail'
    ? `${baseUrl}/api/blog/${blogId}`
    : `${baseUrl}/api/blog/getBlog?pageNo=1&pageSize=10`;
  const response = http.get(url, { tags: { endpoint: mode } });

  let success = false;
  let reason = response.status !== 200 ? `http_${response.status}` : 'invalid_json';
  try {
    const body = response.json();
    const validData = mode === 'list'
      ? Array.isArray(body.data) && (!expectNonempty || body.data.length > 0)
      : body.data != null && body.data.id != null;
    success = response.status === 200 && body.code === 200 && validData;
    reason = response.status !== 200
      ? `http_${response.status}`
      : body.code !== 200 ? `business_${body.code}` : 'invalid_data';
  } catch (_) {
    // Invalid JSON is a business failure too.
  }

  check(response, { 'business success': () => success });
  businessFailures.add(!success);
  if (success) {
    successDuration.add(response.timings.duration);
  } else {
    failureDuration.add(response.timings.duration);
  }
  if (!success) {
    failureReasons.add(1, { reason });
    // Print one sample per VU when diagnosing failures.
    if (__ENV.DEBUG_FAILURES === '1' && !printedFailure) {
      console.error(`failure reason=${reason} status=${response.status} body=${String(response.body).slice(0, 1500)}`);
      printedFailure = true;
    }
  }
}
