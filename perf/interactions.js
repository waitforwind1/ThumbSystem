import http from 'k6/http';
import { check, fail } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8082';
const blogId = __ENV.BLOG_ID;
const prefix = __ENV.USER_PREFIX || 'loaduser';
const password = __ENV.TEST_PASSWORD;
const vus = Number(__ENV.VUS || 10);
const authCookies = __ENV.AUTH_COOKIES_FILE ? JSON.parse(open(__ENV.AUTH_COOKIES_FILE)) : null;
const businessFailures = new Rate('business_failures');
const successfulWrites = new Counter('successful_writes');
const writeDuration = new Trend('write_duration', true);

if (!blogId || !/^\d+$/.test(blogId)) {
  throw new Error('Set BLOG_ID to an existing published blog ID');
}
if (!password) {
  throw new Error('Set TEST_PASSWORD for the seeded test users');
}
if (!Number.isInteger(vus) || vus < 1 || vus > 999) {
  throw new Error('VUS must be an integer from 1 to 999');
}
if (authCookies && (!Array.isArray(authCookies) || authCookies.length < vus ||
    new Set(authCookies.slice(0, vus)).size !== vus)) {
  throw new Error('AUTH_COOKIES_FILE must contain a distinct session for every VU');
}

export const options = {
  noCookiesReset: true,
  scenarios: {
    interactions: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.RATE || 1),
      timeUnit: '1s',
      duration: __ENV.DURATION || '1m',
      preAllocatedVUs: vus,
      maxVUs: vus,
    },
  },
  thresholds: {
    business_failures: ['rate<0.01'],
    dropped_iterations: ['count==0'],
  },
};

let loggedIn = false;
let authCookie = null;
let loggedFailures = 0;

function isSuccess(response) {
  try {
    return response.status === 200 && response.json('code') === 200 &&
      response.json('data') === true;
  } catch (_) {
    return false;
  }
}

function write(path) {
  // The request DTO intentionally spells this property "blogid".
  const response = http.post(
    `${baseUrl}${path}`,
    `{"blogid":${blogId}}`,
    { headers: { 'Content-Type': 'application/json', ...(authCookie ? { Cookie: authCookie } : {}) }, tags: { endpoint: path } },
  );
  const success = isSuccess(response);
  if (!success && __ENV.DEBUG_FAILURES === '1' && loggedFailures++ < 1) {
    console.error(`write failed vu=${__VU} endpoint=${path} status=${response.status} body=${response.body}`);
  }
  check(response, { 'business success': () => success });
  businessFailures.add(!success);
  writeDuration.add(response.timings.duration);
  if (success) successfulWrites.add(1);
  return success;
}

export default function () {
  if (authCookies) {
    authCookie = authCookies[__VU - 1];
    if (!authCookie) fail(`Missing prelogin cookie for VU ${__VU}`);
    loggedIn = true;
  }
  if (!loggedIn) {
    const account = `${prefix}${String(__VU).padStart(3, '0')}`;
    const response = http.post(`${baseUrl}/api/user/login`, {
      account,
      password,
    }, { tags: { endpoint: 'login' } });
    try {
      loggedIn = response.status === 200 && response.json('code') === 200;
    } catch (_) {
      loggedIn = false;
    }
    if (!loggedIn) fail(`Login failed for ${account}`);
  }

  // One iteration makes two write requests. Start with fresh, unliked test accounts.
  if (write('/api/thumb/do')) {
    write('/api/thumb/undo');
  }
}
