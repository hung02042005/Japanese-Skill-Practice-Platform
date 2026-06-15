import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import {
  getFlashcardDecks,
  getFlashcardsByDeck,
  getFlashcardsDue,
  revealFlashcard,
  rateFlashcard,
  addToFlashcard,
} from '../../api/studentService';

// ─── Thunks ──────────────────────────────────────────────────────────────────

export const fetchDecksThunk = createAsyncThunk(
  'flashcard/fetchDecks',
  async (_, { rejectWithValue }) => {
    try {
      return await getFlashcardDecks();
    } catch (err) {
      return rejectWithValue(err?.response?.data?.message ?? 'Không thể tải bộ thẻ.');
    }
  },
);

export const fetchCardsByDeckThunk = createAsyncThunk(
  'flashcard/fetchCardsByDeck',
  async (deck, { rejectWithValue }) => {
    try {
      const deckId = typeof deck === 'object' ? deck.deckId : deck;
      const page = await getFlashcardsByDeck(deckId);
      return { deck, cards: page.content ?? [] };
    } catch (err) {
      return rejectWithValue(err?.response?.data?.message ?? 'Không thể tải thẻ.');
    }
  },
);

export const fetchDueCardsThunk = createAsyncThunk(
  'flashcard/fetchDueCards',
  async (size = 50, { rejectWithValue }) => {
    try {
      const page = await getFlashcardsDue(size);
      return page.content ?? [];
    } catch (err) {
      return rejectWithValue(err?.response?.data?.message ?? 'Không thể tải bộ thẻ ôn tập.');
    }
  },
);

export const revealCardThunk = createAsyncThunk(
  'flashcard/revealCard',
  async (flashcardId, { rejectWithValue }) => {
    try {
      return await revealFlashcard(flashcardId);
    } catch (err) {
      return rejectWithValue(err?.response?.data?.message ?? 'Không thể tải nội dung thẻ.');
    }
  },
);

export const rateCardThunk = createAsyncThunk(
  'flashcard/rateCard',
  async ({ flashcardId, rating, isLastCardInSession = false }, { rejectWithValue }) => {
    try {
      return await rateFlashcard(flashcardId, rating, isLastCardInSession);
    } catch (err) {
      return rejectWithValue(err?.response?.data?.message ?? 'Không thể lưu đánh giá.');
    }
  },
);

export const addCardThunk = createAsyncThunk(
  'flashcard/addCard',
  async ({ contentType, contentId, deckId, deckName }, { rejectWithValue }) => {
    try {
      return await addToFlashcard(contentType, contentId, deckId ?? deckName);
    } catch (err) {
      return rejectWithValue(err?.response?.data?.message ?? 'Không thể thêm thẻ.');
    }
  },
);

// ─── Slice ───────────────────────────────────────────────────────────────────

const flashcardSlice = createSlice({
  name: 'flashcard',
  initialState: {
    // Deck list
    decks: [],
    decksStatus: 'idle',

    // Cards for active deck view
    activeDeck: null,
    deckCards: [],
    deckCardsStatus: 'idle',

    // Review session
    reviewQueue: [],
    reviewStatus: 'idle',
    currentReviewIdx: 0,
    isFlipped: false,
    backContent: null,
    revealStatus: 'idle',
    rateStatus: 'idle',
    isSessionDone: false,
    nextReviewDate: null,

    error: null,
  },
  reducers: {
    setActiveDeck(state, action) {
      state.activeDeck = action.payload;
      state.deckCards = [];
    },
    clearActiveDeck(state) {
      state.activeDeck = null;
      state.deckCards = [];
    },
    flipCard(state) {
      state.isFlipped = true;
    },
    advanceReview(state) {
      state.currentReviewIdx += 1;
      state.isFlipped = false;
      state.backContent = null;
    },
    finishSession(state, action) {
      state.isSessionDone = true;
      state.nextReviewDate = action.payload ?? null;
    },
    resetSession(state) {
      state.reviewQueue = [];
      state.currentReviewIdx = 0;
      state.isFlipped = false;
      state.backContent = null;
      state.isSessionDone = false;
      state.nextReviewDate = null;
      state.reviewStatus = 'idle';
    },
    clearError(state) {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    // fetchDecks
    builder
      .addCase(fetchDecksThunk.pending, (state) => { state.decksStatus = 'loading'; state.error = null; })
      .addCase(fetchDecksThunk.fulfilled, (state, action) => { state.decksStatus = 'succeeded'; state.decks = action.payload ?? []; })
      .addCase(fetchDecksThunk.rejected, (state, action) => { state.decksStatus = 'failed'; state.error = action.payload; });

    // fetchCardsByDeck
    builder
      .addCase(fetchCardsByDeckThunk.pending, (state) => { state.deckCardsStatus = 'loading'; })
      .addCase(fetchCardsByDeckThunk.fulfilled, (state, action) => {
        state.deckCardsStatus = 'succeeded';
        state.activeDeck = action.payload.deck;
        state.deckCards = action.payload.cards;
      })
      .addCase(fetchCardsByDeckThunk.rejected, (state, action) => { state.deckCardsStatus = 'failed'; state.error = action.payload; });

    // fetchDueCards
    builder
      .addCase(fetchDueCardsThunk.pending, (state) => { state.reviewStatus = 'loading'; })
      .addCase(fetchDueCardsThunk.fulfilled, (state, action) => {
        state.reviewStatus = 'succeeded';
        state.reviewQueue = action.payload;
        if (action.payload.length === 0) state.isSessionDone = true;
      })
      .addCase(fetchDueCardsThunk.rejected, (state, action) => { state.reviewStatus = 'failed'; state.error = action.payload; });

    // revealCard
    builder
      .addCase(revealCardThunk.pending, (state) => { state.revealStatus = 'loading'; })
      .addCase(revealCardThunk.fulfilled, (state, action) => {
        state.revealStatus = 'idle';
        state.backContent = action.payload?.backContent ?? action.payload;
        state.isFlipped = true;
      })
      .addCase(revealCardThunk.rejected, (state, action) => { state.revealStatus = 'failed'; state.error = action.payload; });

    // rateCard
    builder
      .addCase(rateCardThunk.pending, (state) => { state.rateStatus = 'loading'; })
      .addCase(rateCardThunk.fulfilled, (state, action) => {
        state.rateStatus = 'idle';
        const nextIdx = state.currentReviewIdx + 1;
        if (nextIdx >= state.reviewQueue.length) {
          state.isSessionDone = true;
          state.nextReviewDate = action.payload?.nextReviewDate ?? null;
        } else {
          state.currentReviewIdx = nextIdx;
          state.isFlipped = false;
          state.backContent = null;
        }
      })
      .addCase(rateCardThunk.rejected, (state, action) => { state.rateStatus = 'failed'; state.error = action.payload; });
  },
});

export const {
  setActiveDeck, clearActiveDeck,
  flipCard, advanceReview, finishSession, resetSession,
  clearError,
} = flashcardSlice.actions;

export default flashcardSlice.reducer;
