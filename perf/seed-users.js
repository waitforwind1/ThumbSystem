import http from 'k6/http';
import { check, fail } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8082';
const prefix = __ENV.USER_PREFIX || 'loaduser';
const password = __ENV.TEST_PASSWORD;
const users = Number(__ENV.USERS || 10);
const start = Number(__ENV.START || 1);

if (!password || password.length < 6 || password.length > 20) {
  throw new Error('Set TEST_PASSWORD to a test-only password of 6-20 characters');
}
if (!Number.isInteger(users) || users < 1 || users > 999) {
  throw new Error('USERS must be an integer from 1 to 999');
}
if (!Number.isInteger(start) || start < 1 || start + users - 1 > 999) {
  throw new Error('START and USERS must select account numbers from 001 to 999');
}
if (!/^[A-Za-z0-9_]+$/.test(prefix) || prefix.length + 3 > 20) {
  throw new Error('USER_PREFIX must be alphanumeric/underscore and leave room for a 3-digit suffix');
}

export const options = {
  scenarios: {
    seed: {
      executor: 'shared-iterations',
      vus: 1,
      iterations: users,
      maxDuration: '10m',
    },
  },
};

export default function () {
  const account = `${prefix}${String(start + __ITER).padStart(3, '0')}`;
  const response = http.post(`${baseUrl}/api/user/register`, {
    account,
    password,
    checkPassword: password,
  });

  let success = false;
  try {
    success = response.status === 200 && response.json('code') === 200;
  } catch (_) {
    // Invalid JSON is a failure.
  }
  check(response, { 'registered': () => success });
  if (!success) {
    fail(`Could not register ${account}; choose a fresh USER_PREFIX for a new run`);
  }
}
