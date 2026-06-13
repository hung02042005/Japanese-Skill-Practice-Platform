import { readFileSync, writeFileSync } from 'fs';

// Windows-1252 special chars (0x80-0x9F) that don't exist in ISO-8859-1
const cp1252 = new Map([
  ['€', 0x80], ['‚', 0x82], ['ƒ', 0x83], ['„', 0x84],
  ['…', 0x85], ['†', 0x86], ['‡', 0x87], ['ˆ', 0x88],
  ['‰', 0x89], ['Š', 0x8A], ['‹', 0x8B], ['Œ', 0x8C],
  ['Ž', 0x8E], ['‘', 0x91], ['’', 0x92], ['“', 0x93],
  ['”', 0x94], ['•', 0x95], ['–', 0x96], ['—', 0x97],
  ['˜', 0x98], ['™', 0x99], ['š', 0x9A], ['›', 0x9B],
  ['œ', 0x9C], ['ž', 0x9E], ['Ÿ', 0x9F],
]);

function fixMojibake(str) {
  const bytes = [];
  for (let i = 0; i < str.length; i++) {
    const ch = str[i];
    const cp = str.charCodeAt(i);
    if (cp <= 0xFF) {
      bytes.push(cp);
    } else if (cp1252.has(ch)) {
      bytes.push(cp1252.get(ch));
    } else {
      // Already correct Unicode (e.g. real Japanese chars) — pass through
      bytes.push(...Buffer.from(ch, 'utf8'));
    }
  }
  return Buffer.from(bytes).toString('utf8');
}

const filePath = new URL('./apps/frontend/src/api/mockData.js', import.meta.url).pathname.slice(1);
const raw = readFileSync(filePath, 'utf8');

// Strip UTF-8 BOM if present
const content = raw.startsWith('﻿') ? raw.slice(1) : raw;

const fixed = fixMojibake(content);

writeFileSync(filePath, fixed, 'utf8');
console.log('Done. Fixed', filePath);
