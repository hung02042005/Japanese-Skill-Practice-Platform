import { useSearchParams } from 'react-router-dom';
import VocabHome from './VocabHome';
import VocabularyList from './VocabularyList';

/**
 * Dispatcher cho route `/vocabulary` (SPEC-vocab-home §1):
 *   - ?view=list  → VocabularyList (màn danh sách từng từ, SPEC-vocabulary)
 *   - mặc định    → VocabHome (entry-point gamified)
 *
 * Phiên flashcard có route riêng `/vocabulary/flashcard` (không còn vào qua ?topic).
 */
export default function VocabularyRoute() {
  const [params] = useSearchParams();
  if (params.get('view') === 'list') return <VocabularyList />;
  return <VocabHome />;
}
