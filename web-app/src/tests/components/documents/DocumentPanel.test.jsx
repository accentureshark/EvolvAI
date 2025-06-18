vi.mock('../../../components/documents/DocumentList', () => ({
  DocumentList: () => <div data-testid="mock-document-list">Mock DocumentList</div>,
}));

import { render, screen } from '@testing-library/react';
import { DocumentPanel } from '../../../components/documents/DocumentPanel';

describe('DocumentPanel', () => {
  it('renderiza correctamente el panel con DocumentList', () => {
    render(<DocumentPanel />);

    expect(screen.getByTestId('mock-document-list')).toBeInTheDocument();
    expect(screen.getByText('Mock DocumentList')).toBeInTheDocument();
  });
});