import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TypingIndicator } from '../../../components/ui/TypingIndicator';

describe('TypingIndicator', () => {
    it('se renderiza correctamente con la clase base', () => {
        render(<TypingIndicator />);
        const indicator = screen.getByRole('listitem');
        expect(indicator).toHaveClass('typing-indicator');
    });

    it('contiene exactamente 3 elementos con clase "dot"', () => {
    const { container } = render(<TypingIndicator />);
    const dots = container.querySelectorAll('.dot');
    expect(dots).toHaveLength(3);
    });
});
