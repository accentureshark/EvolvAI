import { render, screen } from '@testing-library/react';
import { MessagePanel } from '../../../components/chat/MessagePanel';
import { vi } from 'vitest';

vi.mock('../../../components/chat/ChatInterface', () => ({
  ChatInterface: () => <div data-testid="chat-interface">Mock ChatInterface</div>,
}));

vi.mock('../../../components/chat/PromptEditor', () => ({
  PromptEditor: () => <div data-testid="prompt-editor">Mock PromptEditor</div>,
}));

describe('MessagePanel', () => {
  it('renderiza ChatInterface y PromptEditor', () => {
    render(<MessagePanel />);

    expect(screen.getByTestId('chat-interface')).toBeInTheDocument();
    expect(screen.getByTestId('prompt-editor')).toBeInTheDocument();
  });
});
